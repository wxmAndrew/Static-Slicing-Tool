# Static-Slicing-Tool

(class assignment)
Subject: Software Analysis, Summer Semester 2020

The task is to implement the static slicing algorithm, for Java byte code. Program slicing computes a set of program statements, called program slice, that may affect the values at some point of interest, the slicing criterion. In particular to implement an intra-procedural static slicer for Java byte
code. 

The tool takes as input a program 𝑃—an arbitrary Java class file in an arbitrary package available in the class path—and a slicing criterion 𝑆. The slicing criterion 𝑆 of a program 𝑃 is a tuple ⟨𝑠, 𝑉⟩, where 𝑠 is a program statement and 𝑉 is a variable set of 𝑃.

The output of the tool is the static backward slice for the variables in 𝑉 from the method start to the program statement 𝑠.

In practice, various techniques exist to compute a slice. A popular—and straight forward—technique is to use a Program Dependence Graph (PDG). It combines control and data dependencies into one graph structure. To calculate a slice for a slicing criterion 𝑆 on the program’s PDG one has to only traverse backward on the PDG starting at the node of the PDG that corresponds to the criterion’s location.


For full description for the assignment, the used framework, libraries and inputs/ output handling please check the following pdf:
https://github.com/AlaaEzzeldin/Static-Slicing-Tool/blob/master/slicer.pdf
