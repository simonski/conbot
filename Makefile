default_target: usage
.PHONY : default_target upload

# VERSION := $(shell $(PYTHON) setup.py --version)

usage:
	@echo "The conbot Makefile"
	@echo ""
	@echo "Usage : make <command> "
	@echo ""
	@echo "  build                 - builds the jar file and the \`conbot\` command"
	@echo "  clean                 - cleans temp files"
	@echo "  test                  - builds and runs tests"
	@echo "  docker                - builds the 'conbot' image"
	@echo "  push                  - pushes the conbot image to dockerhub as 'conbot/conbot:latest'"
	@echo ""

clean:
	mvn clean

test:
	mvn test

build: 
	mvn clean package install
	cat scripts/main.sh target/conbot.jar > conbot
	chmod +x conbot

coverage:
	mvn jacoco:report
	
docker: build
	docker build -f Dockerfile -t conbot .

docker-multistage:
	docker build -f Dockerfile.Multistage -t conbot .

run:
	docker run --name conbot -d 

push: 
	docker tag conbot conbot/conbot:latest
	docker push conbot/conbot:latest

brew: 
	docker tag conbot conbot/conbot:latest
	docker push conbot/conbot:latest
