# sml

小、可配置维护的、灵活的一套java 类库，框架，易开发可配置扩展程序，在某些场景下可以完全替代spring功能，达到服务精简。

## features 

 * 无依赖：无三方jar包依赖，可独立使用;
 
 * IOC：本身为容器，对象生命周期，依赖管理;
 
 * AOP: 引入切面概念，默认jdk动态代理，也可引入cglib包实现类高级代理，可简单实现日志记录，事务，其它统计类服务;
 
 * jdbc: 对jdbc进行轻量级封装达到快速访问数据库，参考spring-jdbc实现;
 
 * sml: sql标记语言，基于一套标签语法（参考ibatis,mybatis）为动态sql提供执行引擎，可扩展动态开发接口服务;
 
 * el : sml表达示语言，方便对象方法、属性操作访问；
 
 * tools:提供常用工具类：MapUtils,Https,CallableHelper,ClassHelper,QueueManaged,ThreadManaged,MethodProxyFactory...
 
 * 报表支持：有一块基于模型驱动报表引擎，封装对报表模型增删改查操作，可替代mybatis-generator功能，获取更高效的开发，可维护性
 
## 适用场景

 * sql书写较多的应用，sql驱动型服务应用
 
 * 服务频繁更新
 
 * 低配置环境下的服务开发(仅需要分配3m内存即可启动sml-server服务发布rest接口服务)

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
   所有bean注入可通过属性文件也可通过 注解`@Bean`(创建bean),`@Inject`(对象注入),`@Val`(value值注入),`@Init`(初始化方法),`@Stop`(销毁方法),
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

## AOP 功能

> 对常用的动态代理技术，jdk动态，cglib等进行抽象，提供统一的API类
``` java
//切面统一继承抽象类 AbstractAspect 可选择性实现 doBefore,doAfter,doException等方法
Object newObject=newProxyInstance(Object proxyTarget,AbstractAspect ... aspects);
//默认代理对象必须有父级接口返回对象Interface，但如果classpath中有cglib包，则可代理任意对象
```
> 应用事务代理：对datasource对象通过TransactionManager来对事务进行管理
``` java
bean-transactionManager1=--class=org.hw.sml.jdbc.transaction.TransactionManager --p-dataSource=#{datasource1}
aop.bean.transactionManager1.packageMatchs=org.hw.sml.test.transaction.(.*)Impl#do(.*?)

bean-transactionManager2=--class=org.hw.sml.jdbc.transaction.TransactionManager --p-dataSource=#{datasource2}
aop.bean.transactionManager2.packageMatchs=org.hw.sml.test.transaction.(.*)Impl#do(.*?)
```
 
## el 表达示语言
   提供了表达示语言给于java动态语言的特性
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
## jdbc数据库访问
   提供了简单的JdbcTemplate对象操作数据库
``` java
DateSource dataSource;
JdbcTemplate jdbcTemplate=new DefaultJdbcTempate(dataSource);
// execute用来执行sql，不需要返回值
jdbcTemplate.execute("create table t_test(id varchar(32),name varchar(32))")});
// update|batchUpdate 用来执行更新操作返回 int 影响行数
int result=jdbcTemplate.update("update t_test set name=? where id=?",new Object[]{"张三","李四"})});
// query用来执行查询sql，返回各种数据对象，一般pojo自动映射(忽视大小写)，也可实现RowMapper
List<Person> persons= jdbcTemplate.queryForList("select id,name from t_test where name like '%'||?'%'",new Object[]{"d"},Person.class);
```

## sql模板引擎

* 背景mybatis,ibatis书写sql的方便，但调整xml配置文件整体服务需要重新启动；

* 一般访问数据库操作关心：执行sql+参数集+返回结果，针对这三块内容，结合mybatis,ibatis语法进行抽象封装，满足sql动态，返回结果可订制；

* 引擎语法标记 `isEmpty`,`isNotEmpty`,`select`,`jdbcType`,`if`,`smlParam`

* 表达式语言（EL）默认已java内置js表达示语言做为实现引擎; 

> 用法：默认初始化好SqlResolvers 对象 SqlResolvers sqlResolvers=new SqlResolvers(new JsEl()).init();

* 用例1：`isNotEmpty`,`isEmpty`
```sql
   select * from table t where id=1
      <isNotEmpty property="a"> and t.a=#a#</isNotEmpty>
      <isEmpty property="b"> and t.b='default'</isEmpty>
-----------------------
Rst rst=sqlResolvers.resolverLinks(sql,new SMLParams().add("a","v1").add("b",new String[]{"v2","v3","v4"}).reinit());
//rst.sqlString---->select * from table t where t.a=? and t.b in(?,?,?)
//rst.params----->[v1, v2, v3, v4]
```
* 用例2：`jdbcType`,`if`( test表达示前后必须留一空格，里面填js表达示，@param 对应的参数值)
```sql    
select * from t_class tc,t_student ts 
	where tc.class_id=ts.class_id
      	<isNotEmpty property="className"> and tc.class_name like '%'||#className#||'%'</isNotEmpty>
      	<isNotEmpty property="sIds"> and ts.s_id in(#sIds#)</isNotEmpty>
	<jdbcType name="sIds" type="array-char">'200802190210'+'@value'</jdbcType>   
	<if test=" '@classId'!='00' "> and tc.class_id=#classId#</if>
---------------------------
Rst rst=sqlResolvers.resolverLinks(sql,new SMLParams().add("className","武术").add("sIds","1001,1002,1003").add("classId","05").reinit());
//rst.sqlString----->select * from t_class tc,t_student ts where tc.class_id=ts.class_id and tc.class_name like '%'||?||'%' and ts.s_id in(?,?,?) and tc.class_id=?
//rst.params----->[武术, 2008021902101001, 2008021902101002, 2008021902101003, 05]

```

 ## Tools

> httpclient,基于底层API+协议,实现对http|https常用请求，包含连接保持，代理，权限认证，乱码处理，多文件多参数上传，下载
* get请求保 持连接，返回utf-8编码
```java
String result=Https.newGetHttps("http://www.baidu.com").keepAlive(true).charset("utf-8").execute();
```
* post form表单提交 可url带参与formparam同时存在
```java
Https https=Https.newPostFormHttps("http://test/w?a=2");
https.getParamer().add("formParam1","1").add("formParam2","2");
https.execute();
```
* post  body请求 body(byte[]|string)
```java
result=Https.newPostHttps("http://test?a=1").body("{a:b,c:d}").execute()
```
* 下载，将请求返回二进制流写入bos 本地流
```java
Https.newGetHttps("http://www.baidu.com").bos(new FileOutputStream("/tempfile")).execute();
```
* 上传，可提交多个文件和多个formparam  body(UpFile) upFile对象可填多个
```java
Https https=Https.newPostHttps("http://test/helloworld/import").upFile().body(Https.newUpFile("t.xlsx",new FileInputStream("D:/temp/t.xlsx")));
https.getParamer().add("a","参数1");
https.getParamer().add("b","参数2");
https.execute();
```
* failover功能，可指定多个url,默认针对连接异常等进行故障转移,可设置重试机制
```java
Https.newGetHttps("http://url1").failover(Https.Failover("https://url2",5,100L)).execute();//多指定一个url,设置重试5次，每次间隔100ms
```

   
 ## ext-httpServer功能  50kb
      提供内置httpServer，为微服务体系提供基础。sml-module 提供一套接口rest开发功能，开发注解类配置类rest风格接口，整体不到1m依赖。
