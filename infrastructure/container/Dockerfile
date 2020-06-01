FROM ubuntu:18.04

RUN apt-get update -y && \
  apt-get upgrade -y && \
  apt-get install -y vim curl maven git unzip

RUN groupadd -r updater && useradd -r -g updater updater
COPY ./run.sh /home/updater/run.sh
RUN mkdir /home/updater/.ssh
COPY ./id_rsa.yed-aws-palettes /home/updater/.ssh/id_rsa

RUN chown -R updater:updater /home/updater
USER updater
WORKDIR /home/updater

CMD [ "/home/updater/run.sh" ]
