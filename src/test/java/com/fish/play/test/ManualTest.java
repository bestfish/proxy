package com.fish.play.test;

import com.fish.play.proxy.ProxyDispatcher;
import junit.framework.TestCase;


public class ManualTest extends TestCase{
	
	public void testProxy() {
		new ProxyDispatcher().init();
	}
}
