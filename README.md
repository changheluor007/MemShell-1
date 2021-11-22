# 免杀Tomcat Filter型内存马

## 介绍
实现了一种比较顽固的内存马：
1. 随机给一个合理的filter名称
2. 动态生成合理filter类名的字节码
3. 将字节码写入classpath中合理的路径
4. 自动给web.xml中加入合理的filter配置

理论上可以做到这样的效果：

如果防御人员对后端业务逻辑和代码没有比较深入的掌握，大概率无法查出内存马的，哪怕借助工具也很难查杀

## 使用
功能：
1. JSP的触发方式：使用`mem.jsp`
2. `HelloServlet`模拟了反序列化的触发方式
3. 添加了`c0ny1`师傅的`scan.jsp`做检测

如何使用：
1. 配置该项目为Tomcat项目并启动
2. 访问`/mem.jsp`或`/hello-servlet`即可注册免杀内存马
3. 访问`/scan.jsp`查看检测结果

## 效果

看起来和真实的`Filter`没有区别，存在真正的class文件
![](https://github.com/EmYiQing/MemShell/blob/master/img/0065.png)

在`web.xml`配置文件中也真实存在
![](https://github.com/EmYiQing/MemShell/blob/master/img/0066.png)
