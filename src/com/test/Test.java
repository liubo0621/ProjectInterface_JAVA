package com.test;

import com.project.Project;
import com.project.Project.TaskStatus;


/**
 * @author Boris
 * @description 
 * 2016��9��21��
 */
public class Test {
	public static void main(String[] args) {
		Project.getInstance().writeTaskMsg(1, "test", 12, TaskStatus.DOING, 20);
	}
}
