import csv
import json
import os
import pprint
from shutil import copyfile
import subprocess
import sys
import xml.etree.ElementTree as ET
from collections import OrderedDict
from typing import Dict, Tuple, Union


def _print_red(s: str) -> None:
    print(f"\033[91m{s}\033[m")


def _execute(
    task_file: Union[bytes, str, os.PathLike],
    project_root: Union[bytes, str, os.PathLike],
    tmp_result_path: Union[bytes, str, os.PathLike],
    java_bin: Union[bytes, str, os.PathLike] = "/usr/bin/java",
) -> None:
    jar_name = "slicer-1.0-SNAPSHOT.jar"
    jar_path = os.path.join(project_root, "build", "libs")

    with open(task_file) as data_file:
        data = json.load(data_file)

    assert data is not None

    results = {}

    for task in data["tasks"]:
        task_name = task["name"]
        package = task["package"]
        class_name = f"{package}.{task['class']}"
        method_name = task["method"]
        line_number = task["line"]
        variable_name = task["variable"]
        agent_string = "=".join([
            os.path.join(jar_path, jar_name),
            package,
        ])
        class_path = "-cp {}".format(
            os.path.join(jar_path, jar_name)
        )
        if "class_path" in task:
            class_path = "{}:{}".format(class_path, task["class_path"])
        xml_path = os.path.join(tmp_result_path, task_name + ".xml")

        app_params = [
            java_bin,
            f"{class_path}",
            "de.uni_passau.fim.se2.SlicerMain",
            f"-c {class_name}",
            f"-m \"{method_name}\"",
            f"-l {line_number}",
            f"-v {variable_name}",
            "-x",
            f"-t {xml_path}",
        ]
        params = " ".join(app_params)

        process = subprocess.Popen(
            params,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            shell=True,
        )
        out, err = process.communicate()


def _get_coverage(
    project_root: Union[bytes, str, os.PathLike]
) -> Tuple[float, float]:
    current_dir = os.getcwd()
    os.chdir(project_root)
    command_line = "./gradlew assemble test"
    process = subprocess.Popen(
        command_line,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        cwd=project_root,
        shell=True,
    )
    out, err = process.communicate()
    os.chdir(current_dir)

    if err.decode("utf-8") != "":
        print(err.decode("utf-8"))
        branch_cov = 0.0
        line_cov = 0.0
    else:
        if not os.path.exists(
            os.path.join(project_root, "build", "jacoco", "csv")
        ):
            _print_red("Coverage CSV not found")
            return 0.0, 0.0
        with open(
            os.path.join(project_root, "build", "jacoco", "csv"),
            mode="r",
        ) as csv_file:
            def omit(package: str, class_name: str) -> bool:
                omit_pairs = [
                    ("line_coverage", "LineCoverageMain"),
                    ("line_coverage.instrumentation", "Agent"),
                    ("line_coverage", "OutputWriter"),
                    ("examples", "Calculator"),
                    ("examples", "CalculatorTest"),
                    ("examples", "Lift"),
                    ("examples", "LiftTest"),
                    ("examples", "Maximum"),
                    ("examples", "MaximumTest"),
                ]
                for p, c in omit_pairs:
                    p = f"de.uni_passau.fim.se2.{p}"
                    if package == p and class_name == c:
                        return True
                return False

            csv_reader = csv.DictReader(csv_file)
            branches_covered = 0
            branches_missed = 0
            statements_covered = 0
            statements_missed = 0
            for row in csv_reader:
                if not omit(row["PACKAGE"], row["CLASS"]):
                    branch_missed = int(row["BRANCH_MISSED"])
                    branch_covered = int(row["BRANCH_COVERED"])
                    stmt_missed = int(row["INSTRUCTION_MISSED"])
                    stmt_covered = int(row["INSTRUCTION_COVERED"])

                    branches_covered += branch_covered
                    branches_missed += branch_missed
                    statements_covered += stmt_covered
                    statements_missed += stmt_missed

            if branches_missed + branches_covered > 0:
                branch_cov = branches_covered / (branches_covered + branches_missed)
            else:
                branch_cov = 0.0
            if statements_missed +statements_covered > 0:
                line_cov = statements_covered / (statements_covered + statements_missed)
    return line_cov, branch_cov


def _compare_files(
    expected_file: Union[bytes, str, os.PathLike],
    result_file: Union[bytes, str, os.PathLike],
    error_file_path: Union[bytes, str, os.PathLike],
    error_expected_file: Union[bytes, str, os.PathLike],
    error_actual_file: Union[bytes, str, os.PathLike],
):

    def extract_results(root):
        results = OrderedDict()
        for line in root.findall("line"):
            line_number = line.get("nr")
            line_id = line.get("id")
            line_instruction = line.get("instruction")
            results[f"{line_number}.{line_id}"] = line_instruction
        return results

    good = "✔"
    bad = "✘"
    fishy = "∅"
    expected_root = ET.parse(expected_file).getroot()
    expected_results = extract_results(expected_root)
    try:
        result_root = ET.parse(result_file).getroot()
    except FileNotFoundError as e:
        _print_red(pprint.pformat(e))
        os.makedirs(error_file_path, exist_ok=True)
        copyfile(expected_file, error_expected_file)
        return bad

    actual_results = extract_results(result_root)

    if expected_results == actual_results:
        return good
    else:
        os.makedirs(error_file_path, exist_ok=True)
        copyfile(expected_file, error_expected_file)
        copyfile(result_file, error_actual_file)
        return bad


def _compare_results(
    expected_path: Union[bytes, str, os.PathLike],
    tmp_result_path: Union[bytes, str, os.PathLike],
    task_file: Union[bytes, str, os.PathLike],
    error_file_path: Union[bytes, str, os.PathLike],
) -> Dict[str, str]:
    results = {}

    with open(task_file) as data_file:
        data = json.load(data_file)

    assert data is not None

    for task in data["tasks"]:
        task_name = task["name"]
        expected_file = os.path.join(expected_path, task_name + ".xml")
        result_file = os.path.join(tmp_result_path, task_name + ".xml")
        error_expected_file = os.path.join(error_file_path, task_name + "_expected.xml")
        error_actual_file = os.path.join(error_file_path, task_name + "_actual.xml")

        result = _compare_files(
            expected_file, result_file, error_file_path, error_expected_file, error_actual_file
        )

        results[task_name] = result

    return results


def execute(
    expected_path: Union[bytes, str, os.PathLike],
    task_file: Union[bytes, str, os.PathLike],
    project_root: Union[bytes, str, os.PathLike],
    tmp_result_path: Union[bytes, str, os.PathLike],
    error_file_path: Union[bytes, str, os.PathLike],
) -> Dict[str, Union[float, str]]:
    results = {}

    stmt_cov, branch_cov = _get_coverage(project_root)
    results["stmt_cov"] = stmt_cov
    results["branch_cov"] = branch_cov
    avg_cov = (stmt_cov + branch_cov) / 2
    print(f"    Line Cov: {stmt_cov}, Branch_Cov: {branch_cov}, Avg: {avg_cov}")

    _execute(task_file, project_root, tmp_result_path)

    results.update(_compare_results(expected_path, tmp_result_path, task_file, error_file_path))

    pprint.pprint(results)

    bad = "✘"
    fishy = "∅"
    for _, value in results.items():
        if value == bad or value == fishy:
            return 1
    return 0


def main():
    current_dir = os.path.dirname(os.path.abspath(__file__))
    expected_path = os.path.join(
        current_dir, "..", "expected-results"
    )
    task_file = os.path.join(current_dir, "tasks.json")
    project_root = os.path.join(current_dir, "..")
    tmp_result_path = "/tmp"
    return execute(
        expected_path, task_file, project_root, tmp_result_path, tmp_result_path
    )


if __name__ == "__main__":
    sys.exit(main())

