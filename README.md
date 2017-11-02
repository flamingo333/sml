# sml
小、可配置维护的、灵活的一套java 类库，框架（200kb+），易开发可配置扩展程序。
## features 

 * 无依赖：无三方jar包依赖，可独立使用
 
 * IOC：本身为容器，对象生命周期，依赖管理
 
 * AOP: 引入切面概念，默认jdk动态代理，也可引入cglib包实现类高级代理，可简单实现日志记录，事务，其它统计类服务。
 
 * jdbc: 对jdbc进行轻量级封装达到快速访问数据库，参考spring-jdbc实现
 
 * sml: sql标记语言，基于一套标签语法（参考ibatis,mybatis）为动态sql提供执行引擎，可扩展动态开发接口服务
 
 * el : sml表达示语言，方便方法、对象操作访问
 
 * tools:提供常用工具类：MapUtils,Https,CallableHelper,ClassHelper,QueueManaged,ThreadManaged,MethodProxyFactory...
 
 * 报表支持：有一块基于模型驱动报表引擎，封装对报表模型增删改查操作，可替代mybatis-generator功能，获取更高效的开发，可维护性
 
## 适用场景

 * sql书写较多的应用，sql驱动型服务应用
 
 * 服务频繁更新
 
 * 低配置环境下的服务开发

##Getting started
###Add the maven dependency
```xml
<dependency>
    <groupId>org.hw.sml</groupId>
    <artifactId>sml</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
###add repository
```xml
<repository>
   <id>hw-snapshots</id>
   <name>hw-snapshots</name>
   <url>http://23.247.25.117:8081/nexus/content/repositories/snapshots</url>
</repository>
```
## IOC 功能
没有xml，完全通过属性文件进行bean的生命周期管理，默认属性文件：`sml.properties`
```html
   person.age=25
   bean-doubleBean=--class=java.lang.Double(12d)
   bean-person=--class=org.hw.sml.test.Person --p-age=${person.age} --p-height=#{doubleBean} --init=init --destroy=stop
```
   在属性通过`${*}` 赋值 `#{*}`赋对象 ,`--init=`后面为bean初始化方法，`destroy`为对象销毁执行操作，
   所有bean注入可通过属性文件也可通过 注解`@Bean`,`@Inject`,`@Val`,`@Init`,`@Stop`
```java
   @Bean
   public class Person{
        @Val("${person.id}")
   	private String id;
	@Val("${person.name}")
	private String name;
	@Val("${person.age}")
	private int age;
	@Inject("doubleBean")
	private Double height;
	@Val("['成功','失败']")
	private List<String> infos;
 	@Init(delay=true,sleep=5)//对象创建5s后执行
	public void init(){
	}
	@Stop  //程序退出，对象销毁执行
	public void destroy(){
	}
   }
```
## el 表达示语言
   提供了强大的表达示语言给于java动态语言的特性
```java
    public void test(){
    	ElContext el=new SmlElContext().withBeanMap(beanMap).withPropertiesMap(properties).init();
	//beanMap   Map<String,Object> obj is bean,properties Properties对象，这两参数就指定了表达示依赖的上下文环境
        el.evel('a');//'a'----->String.class "a"
	el.evel(12.0d);//12.0d----->double.class  12.0d|12i ----->int.class 12|12l ---->long.class 12l
	el.evel({id:'1001',age:25,name:'zhangsan'})// ----->Map<String,Object>.class 
	el.evel([1,2,3,4]) //--List.class 
	el.evel(${person.age})//return properties.[person.age] return 25
	el.evel(#{person})//return beanMap.[person] person bean
	el.evel(#{person.setAge(25i)})//给person  对象属性age进行赋值
	el.evel(#{person.infos.contains('成功')})// 人物标签是否包含'成功'
	//复杂的表达示 如果为一个对象跟参数或者同类有冲突可能过`()`进行重新定义
	el.evel(#{({a:1,b:({c:2,d:3})}).get('a')})
	//#{}默认为beanMap中查询bean,如果带`()`刚把已带内容当成一个对象处理
	//b：赋值对象为一个对象所以，所以通过`()`进行转义
	//超复杂表达示like下面，自行理解。
	el.evel({a:({b:0i,c:({d:1i,e:({f:2i,g:({h:3i,i:({j:4i,k:({l:5i,m:({n:6i,o:${server.port},p:({q:#{smlBeanHelper.beanMap},e:#{smlPropertiesHelper.propertiesMap.get(('server.port'))}})})})})})})})})});
    }
```
## jdbc 数据库访问
   提供了简单的JdbcTemplate对象操作数据库，结合标签语法为动态sql提供执行引擎
   
* mybatis,ibatis书写sql的方便，但调整xml配置文件整体服务需要重新启动

* 一段完整的sql查询包括    查询sql+参数集

* marks `isEmpty`,`isNotEmpty`,`select`,`jdbcType`,`if`

* elp    jsEl,spl implements El interface; 

 example sql
```sql
      select * from table t where 1=1 
      <isNotEmpty property="a"> and t.a=#a#</isNotEmpty>
      <isNotEmpty property="b"> and t.b in(#b#)</isNotEmpty>
```
code
```java
             SqlResolvers sqlResolvers=new SqlResolvers(new JsEl());
		sqlResolvers.init();	
		Rst rst=sqlResolvers.resolverLinks(sql,new SMLParams().add("a","v1")
		.add("b",new String[]{"v2","v3","v4"}).reinit());
		System.out.println(rst.getSqlString());//print sqlString
		System.out.println(rst.getParamObjects());//print params
```	
result
```html
	  sqlString:select * from table t where t.a=? and t.b in(?,?,?)
	  params   :[v1, v2, v3, v4]
```
multi-mark example sql
```sql    
    	select * from t_class tc,t_student ts 
	where tc.class_id=ts.class_id
      	<isNotEmpty property="className"> and tc.class_name like '%'||#className#||'%'</isNotEmpty>
      	<isNotEmpty property="sIds"> and ts.s_id in(#sIds#)</isNotEmpty>
	<jdbcType name="sIds" type="array-char">'200802190210'+'@value'</jdbcType>   
	//jdbcType标签 动态改变值的类型及值
	<if test=" '@classId'!='00' "> and tc.class_id=#classId#</if>
```
code
```java
	Rst rst=sqlResolvers.resolverLinks(sql,new SMLParams().add("className","武术")
	.add("sIds","1001,1002,1003").add("classId","05").reinit());
```		
result
```html	
	select * from t_class tc,t_student ts where tc.class_id=ts.class_id 
	and tc.class_name like '%'||?||'%' and ts.s_id in(?,?,?) and tc.class_id=?
        [武术, 2008021902101001, 2008021902101002, 2008021902101003, 05]
```
### tag
```html
<isNotEmpty property="param">
		//todo
</isNotEmpty>
```
   param为查询参数，判断是否为空

```html
   	<if test=" '@param'=='true' ">
		//todo
	</if>
```
   test表达示前后必须留一空格，里面填js表达示，@param  对应的参数值
   
```html
   	<select id="table_choose">
		//todo
	</select>	
	<included id="table_choose"/>
```
   上面两类内容进行替换
 ## https Http Client功能
      底层API+协议,实现对http常用请求，包含连接保持，权限认证，乱码处理，多文件多参数上传，下载
```java
	//get请求保持连接，返回utf-8编码
	String result=Https.newGetHttps("http://www.baidu.com").keepAlive(true).charset("utf-8").execute();
	//post form表单提交 可url带参与formparam同时存在
	Https https=Https.newPostFormHttps("http://test/w?a=2").execute();
	https.getParamer().add("formParam1","1").add("formParam2","2");
	https.execute();
	//post  body请求 body(byte[]|string)
	result=Https.newPostHttps("http://test?a=1").body("{a:b,c:d}").execute()
	//下载，将请求返回二进制流写入bos 本地流
	Https.newGetHttps("http://www.baidu.com").bos(new FileOutputStream("/tempfile")).execute();
	//上传，可提交多个文件和多个formparam  body(UpFile) upFile对象可填多个
	Https https=Https.newPostHttps("http://test/helloworld/import").upFile().body(Https.newUpFile("t.xlsx",new FileInputStream("D:/temp/t.xlsx")));
	https.getParamer().add("a","参数1");
	https.getParamer().add("b","参数2");
	https.execute();
```
   
 ## ext-httpServer功能  50kb
      提供内置httpServer，为微服务体系提供基础。sml-module 提供一套接口rest开发功能，开发注解类配置类rest风格接口，整体不到1m依赖。
