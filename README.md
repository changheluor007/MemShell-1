# 免杀Tomcat Filter型内存马

功能：
1. JSP的触发方式：使用`mem.jsp`
2. `HelloServlet`模拟了反序列化的触发方式
3. 添加了`c0ny1`师傅的`scan.jsp`做检测

如何使用：
1. 配置该项目为Tomcat项目并气动
2. 访问`/mem.jsp`或`/hello-servlet`即可注册免杀内存马
3. 访问`/scan.jsp`查看检测结果
