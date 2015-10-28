
all:
	make dcnet

dcnet: 
	mkdir -p bin
	mkdir -p plots
	javac -d bin/ -cp src/ src/component/Main.java
