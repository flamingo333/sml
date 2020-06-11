## 上海报表配置模块接口

---

### 接口说明

1. 根据常见的报表思想，实现的一套报表后台接口，只需要通过配置可达到，单表，或视图的，改善，支持组合（表多字段组成的一个新字段）返回，
   查询条件上支持like,not like,ilike,in,组合字段查询，返回样式也可根据数据库函数达到
   比较友好的体验。对于报表所返回的接口支持聚合函数返回图表数据。
   对配置类不需要开发，可直接进行配置达到快速使用增删改查功能的效果

2. `base_url`=http://localhost:8080/services/ws/rest/query/report_query/ (现网)

### 后台表设计

* `建表语句` ：create table O_CO_BA_CFG_RCPT                          --对象配置表
			(
			id        VARCHAR2(64) not null primary key,  --id
			name      VARCHAR2(256),                      --中文名称
			 rcpt_name VARCHAR2(256),                     --表视图名称
			 db_id     VARCHAR2(64)                       --数据库标识
			)
		create table O_CO_BA_CFG_RCPT_DETAIL                  --对象-字段配置表
			(
			  id                NUMBER(10),               --序号用于排序
			  rcpt_id           VARCHAR2(64),             --对象id关联上表
			  kpi_name_ch       VARCHAR2(200),	      --字段中文   对于表头
			  kpi_name_en       VARCHAR2(50),	      --字段英文
			  format            VARCHAR2(256),	      --函数格式化，可填写数据库函数，或多列字段组合
			  field_type        VARCHAR2(256),	      --字段类型
			  enabled           NUMBER(1) default 1,      --是否启用
			  length            VARCHAR2(256),	      --列长，供界面使用
			  for_update        NUMBER,		      --下面为对应导入更新查询需要用到标识字段
			  for_insert        NUMBER default 1,
			  for_import        NUMBER default 1,
			  for_import_update NUMBER,
			  for_query         NUMBER default 1,
			  is_query          NUMBER default 0
			)
### 核心方法

* `List<Map<String,Object>>  query(String id, ParamCriteria pc)`  获取当前页数据
* `Result getResult(final String id, final ParamCriteria pc)`    获取当前分页需要集包括当前页数据，当前总条数
* `int update(String id, ParamCriteriaForUpdate pcu)`     更新-增删改
* `int importReport(String id, String type,List<Map<String,Object>> datas)`  模板导入   type [insert,update,adu]
### jar包引用
	建议springxml配置
```
	<bean id="rcptCommonService" class="com.eastcom_sw.inas.core.service.report.ReportCommonService" init-method="init">
		<property name="dss">
			<map>
				<entry key="defJt" value-ref="ipmsdmDataSource"></entry>   <!--架构表结构默认库defJt-->
				<entry key="inas" value-ref="ipmsdmDataSource"></entry>	   <!--建立 key（数据库db_id值）-value 多库关系-->
			</map>
		</property>
	</bean>
```
	引入后便可直接使用    rcptCommonService.核心方法
### 根据id获取报表相关配置信息

* `GET {reportId}/config`

	* `查询参数` reportId 报表id

	* `返回结果`
	    

    {
        "resultCode": 0,
        "message": "success",
        "id": "001",
        "tableName": "INAS_FN_JT_LTE_SUGGESTION_2G_HV_CELL_D",
        "reportName": "2G高回流小区_规划选址/网络优化",
        "result":
        [
            {
                "field": "TIMEID",
                "head": "时间"
            },
            {
                "field": "LACCI",
                "head": "LAC-CI"
            }
	    ...
        ]
    }





### 查询

* `POST {reportId}/query`

	 * `查询参数`: reportId 报表id
			 {
			 "startIndex":0,
			 "rowPerPage":10,
			 "conditionMap":{"TIMEID":[{"operator":"<=","value":"201506100000"}],"SD_NAME":[{"operator":"in","value":"东,西区分公司"}],"KPI_003":[{"operator":"<","value":"20145"}]},
			 "intendedFields":[],"orderByFields":["TIMEID","SD_NAME"],
			 "orderByType":"desc"
			 }
		* startIndex 查询开始
		* rowPerPage 每页面显示
		* conditionMap 所有返回字段都支持查询  operator[=,>=,<,<=,in,like,not in,not like]等
		* intendFields 返回字段选择默认全返回，也可指定
		* orderByFields 排序
		* orderByType  [asc desc] 默认asc 

	 * `返回结果`
			  {
			  "resultCode": 0,
			  "message": "success",
			  "result": {
			  "count": 6,
			  "datas": [
			      {
				"TIMEID": "2015-01-01 00:00:00",
				"LACCI": "A11_4",
				"CELL_NAME": "新南山HL1H_3",
				"SD_NAME": "西区分公司",
				"COVERAGE": "宏站",
				"KPI_001": 1,
				"KPI_002": 36144.25,
				"KPI_003": 17676.16,
				"KPI_004": 20932.08,
				...
			      },
			      {
				"TIMEID": "2015-01-01 00:00:00",
				"LACCI": "A22_2",
				"CELL_NAME": "万科国际-1楼AL1W_2",
				"SD_NAME": "西区分公司",
				"COVERAGE": "宏站",
				"KPI_001": 1,
				"KPI_002": 36373.8,
				"KPI_003": 15654.79,
				"KPI_004": 45556.18,
				...
				}
				...
			    ]
			}


### 更新

* `POST {reportId}/update`

	 * `查询参数`: reportId 报表id
			 {
			 "type":"update",
			 "updateCondition":{"ID":"101"},
			 "updateField" :{"NAME":"本网率","UPPERLIMIT":"3"}
			 }
		* type 更新操作类型[update,insert,delete]
		* updateCondition 需要更新的条件，包含修改条件删除条件
		* updateField 需要更新的字段，包括修改字段，新增的字段
		

	 * `返回结果`
			  {
			   "resultCode": 0,
			   "message": "success",
			   "size": 1
			  }
		 * size 更新记录数

### 报表导出
* `POST {reportId}/export`
	*查询参数见查询
	*返回结果：url



### chart查询
* `POST {reportId}/chart`

	* `查询参数`: reportId 报表id
				 {
				 "startIndex":0,
				 "rowPerPage":10,
				 "conditionMap":{"TIMEID":[{"operator":"<=","value":"201506100000"}],"SD_NAME":[{"operator":"in","value":"东,西区分公司"}],"KPI_003":[{"operator":"<","value":"20145"}]},
				 "intendedFields":[],"orderByFields":["TIMEID","SD_NAME"],
				 "orderByType":"desc",
				 "chart":{"funcs":{"KPI_009":"sum(KPI_009)","KPI_008":"cast( avg(KPI_008) as numeric(30,2))" },"groupid":["TIMEID"]}
				 }
		* conditionMap 所有返回字段都支持查询  operator[=,>=,<,<=,in,like,not in,not like]等
		* intendFields 返回字段选择默认全返回，也可指定
		* orderByFields 排序
		* orderByType  [asc desc] 默认asc 
		* chart funcs聚合函数集,groupid查询分组id

	 * `返回结果`
			  {
			  "resultCode": 0,
			  "message": "success",
			  "result": {
			  "datas": [
			      {
				"TIMEID": "2015-01-01 00:00:00",
				"KPI_004": 20932.08,
				...
			      },
			      {
				"TIMEID": "2015-01-01 00:00:00",
				"KPI_004": 45556.18,
				...
				}
				...
			    ]
			  }



### 目前已配置好报表、配置
   四网协同相关报表查询导入导出，模板导入导出，性能相关配置功能。










### 修订历史

> 2015-06-12 [黄文] 初始版本设计

> 2015-12-02 [黄文] 添加表结构，核心方法类使用
