package com.sheca.zhongmei.adapter;

public class Entity {

    //根据数组的长度、每页item数量，确定viewpager的页数
    public  int getPageNum(int length,int numberOfEveryPage){//数组长度、每页item数量
        int n=1;
        if(length>numberOfEveryPage){
        	if(numberOfEveryPage == 1)
        		 return length;

            while(length-numberOfEveryPage>=0){
                n++;
                length=length-numberOfEveryPage;
            }
            return n;
        }
        return n;
    }
}
