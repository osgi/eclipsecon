package hello.impl;

import hello.service.HelloService;

public class HelloServiceImpl implements HelloService{

	public String sayHello() {
		return "Hello Spring World!! ";
	}

	public String sayGoodbye() {
		return "Goodbye Spring World!!";
	}
}
