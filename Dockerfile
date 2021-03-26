FROM openjdk:11-sid
RUN apt-get update -y && apt-get install python3.8 -y
COPY . /usr/src/slicer
WORKDIR /usr/src/slicer

CMD ["python3.8", "evalscripts/executor.py"]
