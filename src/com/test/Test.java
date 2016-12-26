package com.test;

import com.project.Project;
import com.project.Project.TaskStatus;


/**
 * @author Boris
 * @description 
 * 2016年9月21日
 */
public class Test {
	public static void main(String[] args) {
		int i = 0;
		Project.getInstance().isDebug(true);
		while(i < 10){
			Project.getInstance().writeTaskMsg(i, "测试" + i, 10, TaskStatus.DOING, 20 + i);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Project.getInstance().writeTaskMsg(i, "测试" + i, 10, TaskStatus.DONE, 20 + i);
			i++;
		}
		try{
			int n = 1/0;
		} catch (Exception e) {
			Project.getInstance().writeTaskMsg(i, "测试异常" + i, 10, TaskStatus.DOING, 20 + i, e);
		}
	}
}
