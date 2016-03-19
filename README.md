# playfish
playfish 基于JAVA技术的网页内容智能抓取
基于JAVA技术的网页内容智能抓取
架构
完全基于java的技术
核心技术
XML解析，HTML解析，开源组件应用。应用的开源组件包括：

	DOM4J：解析XML文件
	jericho-html-2.5：解析HTML文件
	commons-httpclient：读取WEB页面内容工具

其他必须的辅助引用包括：
	commons-codec
	commons-logging
	jaxen
基本业务流程描述
	通过XML文件定义抓取目标
	通过DOM4J开源组件读取XML配置文件
	根据配置文件执行抓取任务
	对抓取到的内容根据定义进行解析与处理
目前缺陷功能描述
	抓取内容如果有分页，则无法获取下一分页
	目标页面可能包含有想抓取的信息，但没有抓取的配置选项。如百度贴吧
	目标页面链接定义不够灵活，对于百度贴吧的链接参数以50增加的只能手动定义
	没有多线程支持
	日志输出比较混乱
	错误处理比较简单，不能对失败的任务自动重新执行，很多地方缺乏错误判断，如空数组
	程序架构不够清晰，代码略显凌乱
	不支持抓取结果存入多个表，目前只能将结果放在一个表中
	是否考虑写一个XML类，提供通用的调用XML节点跟解析结果的方法？
	规定任务文件的DTD？
	通配符替换不支持多个替换
目前功能描述
抓取目标定义
作用：
    批量的定义抓取目标，自动生成要抓取的网页的地址。系统通过这个定义，自动生成要抓取的目标。网页抓取的意义在于批量自动化抓取，不可能手动的输入网址一个个进行抓取，所以抓取目标的自动生成是必须的。
抓取目标定义的XML文件范例：
<target encode="UTF-8" timeout="8000">
	<multi-url>
		<single-url href="http://robbin.javaeye.com/?page=1" />
	</multi-url>
	<wildcard-url href="http://robbin.javaeye.com/?page=(*)"
		startpos="1" endpos="3" />
	<target-regex root="http:// robbin.javaeye.com">
		<![CDATA[
				href\=\'(/blog/\d*)\'
		]]>
	</target-regex>
</target>
XML规则：
1.	必须包含target节点，target的encode与timeout属性如果采用默认，可以不设置
2.	multi-url与wildcard-url可以任选一个，也可以并存，最多2个。合理的情况包括：只有一个multi-url，只有一个wildcard-url，一个multi-url和一个wildcard-url。注意，无论wildcard-url跟multi-url的顺序如何，系统都会从multi-url开始执行。
3.	multi-url下包含至少一个single-url元素，通过single-url元素的href属性设置url
4.	wildcard-url属性包括href，startpos，endpos，这3个属性都是必须的。目前只支持（*）通配，url中只能有一个(*)，（*）将会被替换成startpos和endpos之间的数字。
5.	target-regex属性包括root，该属性值将会被添加在通过这个正则匹配得到的url之前。主要针对相对url。相对url需要加上站点的根路径组成完整路径。而如果是绝对url的话，可以将root属性放空。注意，正则表达式需要放在CDATA标签内。因为正则包含特殊字符，必须放在CDATA内，否则很可能导致解析错误。
解析过程：
1.	首先获得页面编码与超时设置，在接下来读取任何html页面的过程中，这2个参数都会被应用。默认的编码为 UTF-8，默认的超时时限为5000ms。如果要抓取的网站访问很慢，响应时间特别长，那么这个超时时限可以相应设置得高一点。如果在target中没有指定这2个值，那么系统会采用默认的编码跟超时设置。
2.	判断是否包含multi-url定义，包含的话调用相应的方法，如果target下定义了target-regex元素，那么multi-url中定义的url会先被读取，然后将读取到的内容使用target-regex中定义的正则表达式进行匹配，将匹配结果作为目标列表返回。如果target下没有定义target-regex，那么将multi-url下所有的url作为目标列表返回。
3.	判断是否包含wildcard-url定义，包含的话调用相应的方法，首先获得起始跟终结位置，替换生成所有通配的url，接下来同multi-url，如果target下定义了target-regex元素，那么所有生成的通配url会先被读取，然后将读取到的内容使用target-regex中定义的正则表达式进行匹配，将匹配结果作为目标列表返回。如果target下没有定义target-regex，那么将所有生成的通配url作为目标列表返回。
举例1：最简单的定义抓取目标
	例如我们抓取百度的首页，显然这是没什么意义的事情，这里只是举例说明抓取目标的定义方法。
<target encode="gb2312">
	<multi-url>
		<single-url href="http://www.baidu.com/" />
	</multi-url>
</target>
	这样就完整定义了抓取地址，对于target，我们定义了encode属性，但是省略了timeout属性，因为系统默认是utf8编码，而百度是gb2312编码，必须指定。而timeout我们默认就可以。
举例2：通配符url定义抓取目标
	上网时候经常可以看到某篇文章，如果比较长，就可以会被分成多页，我们点击下一页翻看，对于这样的url地址一般都是非常有规律的，通常它的url带有一个类似page=1之类的属性。假设我们抓取某篇文章的全部内容，这篇文章地址为www.test.com/?page=1，它总共有3页，地址就是后面page从1变到3。那么我们可以这样定义目标：
<target>
	<wildcard-url href="www.test.com/?page=(*)"
		startpos="1" endpos="3" />
</target>
	我们假设这个网站响应很快，并采用UTF8编码，target的2个属性我们就忽略了。系统会自动解析成www.test.com/?page=1 , www.test.com/?page=2 , www.test.com/?page=3 这样的3个网址。
举例3：multi-url+正则表达式定义抓取目标
	比较经常的情况是，我们要抓取的内容在多个地址上。例如百度贴吧，我们想要某个贴吧前5页的所有的帖子的内容，显然，采用例子一定义所有帖子的url太烦琐，而帖子的url地址又没办法用通配符来表示，这个时候，使用正则抓取是最好的方法。这要求使用者有正则表达式的基础。
		<target encode="gb2312" timeout="4000">
			<multi-url>
				<single-url href="http://tieba.baidu.com/f?z=0&amp;ct=318767104&amp;amp;lm=11&amp;sc=0&amp;rn=50&amp;tn=baiduKeywordSearch&amp;rs3=0&amp;rs4=0&amp;word=java&amp;pn=0" />
				<single-url href="http://tieba.baidu.com/f?z=0&amp;ct=318767104&amp;lm=11&amp;sc=0&amp;rn=50&amp;tn=baiduKeywordSearch&amp;rs3=0&amp;rs4=0&amp;word=java&amp;pn=50" />
				<single-url href="http://tieba.baidu.com/f?z=0&amp;ct=318767104&amp;lm=11&amp;sc=0&amp;rn=50&amp;tn=baiduKeywordSearch&amp;rs3=0&amp;rs4=0&amp;word=java&amp;pn=100" />
			</multi-url>
			<target-regex root="http://tieba.baidu.com">
				<![CDATA[
						class=t\shref=\"(.*?)\"
				]]>
			</target-regex>
		</target>
	首先我们列出3个页面列表，每个页面上大概有50个帖子，通过查看源代码我们看到它的url是这样写的<a class=t href="/f?kz=350025590" target=_blank > ，通过总结规律，我们得出了上面target-regex的正则表达式，系统将通过这个正则去匹配所有符合条件的地址。注意，必须将我们要的地址页就是这里href里面的内容用()括起来，这样系统才会获取这个分组。
	由于百度这里采用的是相对路径，所以我们要设定根节点root，系统匹配完成后会用root加上匹配结果作为目标地址，也就是http://tieba.baidu.com/f?kz=350025590。
	同样的，wildcard-url元素也可以配合target-regex来使用。
清除目标定义
作用：
	将抓取到的目标文件用定义的规则进行裁减。先去掉网页中我们不需要的内容，可能对于后面的解析有帮助。
清除目标定义的XML文件范例：
<cleaner>
	<clean type="head"></clean>
	<clean type="css"></clean>
	<clean type="script"></clean>
	<clean type="tags">
		<![CDATA[
			<table>|</table>|<br />
		]]>
	</clean>
	<clean type="regex">
		<![CDATA[
			<p>.*?</p>
		]]>
	</clean>
</cleaner>
XML规则：
1.	必须包含cleaner节点。clean任务只要是为了提高后面处理任务的执行效率。clean任务先去掉了网页中不会参与解析的内容，可以减少后面执行解析的速度。
2.	clean节点可以任意多个，预定义的type属性包括：head,css,script,tags,regex。clean类型为head的，会将网页内容截断只保留body跟body之间的内容。head之间的内容会被删除。clean类型为css的，会将页面上所有css定义去掉，但是这个清除不包括去掉内联的style定义。clean类型为script的，会将页面上所有javascript脚本去掉。类型为tags的，如果CDATA内没有定义任何字段，那么会清除页面上所有的html标签，如果CDATA定义了字段，那么会清除定义的html标签。最后一个也是最强大的一个，正则表达式类型，系统会对所有匹配的内容进行正则替换。
解析过程：
1.	读取clean列表，获得要进行清除的任务。
2.	判断clean类型，执行相应的操作。注意：由于clean会对内容进行全文搜索与替换，在内容很长的情况下可能会非常消耗资源并要执行很长时间。对于clean操作如非必要，慎用。只有head类型的是例外，head由于只在头尾出现，所以匹配执行速度很快。是比较常用的定义。
举例1：
	对于抓取，我们通常只关心网页的文字内容。那么解析前我们就可以将head部分的内容都去掉。这部分内容对于解析毫无意义。那么我们可以这样定义：
<cleaner>
			<clean type="head"></clean>
</cleaner>
	很简单完成。
举例2：
	可能我们也不想要内容中的脚本，和头。
<cleaner>
			<clean type="head"></clean>
			<clean type="script"></clean>
</cleaner>
	同样简单。
举例3：
	清除网页上所有的span跟pre标签
<cleaner>
	<clean type="tags">
		<![CDATA[
			<span>|<pre>
		]]>
	</clean>
</cleaner>
处理过程定义
作用：
	系统的核心过程，通过定义这个过程，完成对抓取内容的解析。包括保存到数据库，裁减等。
处理过程定义的XML文件范例：
<processer>
	<process flow="true">
		<tag-filter pos="1" key="id" value="main" />
	</process>
	<process flow="false" table="je_article" field="title">
		<regex-filter>
			<![CDATA[
				<div class=\"blog_title\">.*<h3><.*?>(.*?)</a>.*?</div>
			]]>
		</regex-filter>
	</process>
	<process flow="false" table="je_article" field="tags">
		<regex-filter>
			<![CDATA[
				<div class=\"blog_title\">.*?</strong>:(.*?)</div>
				]]>
		</regex-filter>
	</process>
	<process flow="false" table="je_article" field="content">
		<flag-filter>
			<start-flag>
				<![CDATA[
					<div class="blog_content">
				]]>
			</start-flag>
			<end-flag>
				<![CDATA[
					<div class="blog_bottom">
				]]>
			</end-flag>
		</flag-filter>
	</process>
</processer>
XML规则：
1.	必须定义processer元素，至少包含一个process元素。
2.	processer元素包含了所有的process元素，process元素则定义了页面代码的全部处理流程。
3.	process元素包括3个属性，flow，table，field。flow表示流程。即经过这个process元素解析完的内容是作为下一个步骤的内容或者回退到未解析前的内容。取值为true的时候本步骤处理结果作为下步骤处理的材料，本步骤结果不存入数据库。取值为false的时候，本步骤结果只在本步骤使用。下一个解析回退到本步骤解析之前的内容。table属性定义了本步骤处理结果所要存放的数据库表，而fidld属性定义了所对应表中的字段。
4.	每个process下必须包含一个处理流程。目前处理流程有3种，tag-filter，regex-filter，flag-filter。
5.	tag-filter指标签过滤，获得指定标签的内容。tag-filter包括4个属性，pos，key，value，textonly。pos指html标签的位置。不指定的状况下默认为第一个。key指所要获取的标签的属性，包括name，id，class等，当然其他的如width，href，target等属性也支持，支持所有标准html属性。value指这个属性的值。textonly指是否只获得这个标签内部的文本内容。而不包含该标签。
6.	regex-filter指正则过滤，获得符合正则表达式的内容。注意，必须将正则表达式放在CDATA块内。另外，必须将要获得的内容用（）括起来，只有（）内的内容才会被获取。
7.	flag-filter指标志位过滤，获取2个标志位之间的内容。用户必须自己保证开头跟结尾的标志位是唯一的。这样才能保证精确获得想要的内容。获取标志位需要用户查看源代码，并取得唯一的标志。标志位过滤是目前主流的抓取工具提供的最常用的方法。
解析过程：
1.	首先获得所有处理流程
2.	按顺序执行流程。判断流程process的类型flow属性，根据flow属性调用不同的处理方法。为true的时候进行裁减，为false的时候进行解析保存。
3.	根据flow属性进入正式的解析，判断解析元素filter的类型，是target-filter，或者是regex-filter或者是flag-filter，根据不同的解析类型调用不同的解析过程。解析过程参考XML规则。
4.	所有流程执行完后，系统将收集到的所有字段保存到数据库中。
案例
抓取javaeye博客内容
	这个例子演示了如何抓取javaeye上博客的文章。博客地址http://playfish.javaeye.com/ 。抓取之前，我们需要建立一个数据库跟表，只要将范例用的数据库表导入即可。
第一步：目标定义
首先分析页面上的文章的链接的写法
<a href='/blog/179642'>HashMap和Hashtable及HashSet的区别</a>
<a href='/blog/164080'>IE下ZOOM属性导致的渲染问题</a>
<a href='/blog/161365'>Web2.0网站性能调优实践</a>
	通过这些链接的共同点我们可以很容易发现它的文章链接的规律，得出这样的正则表达式：
	href\=\'(/blog/\d*)\'
	注意，我们要获得的只是类似于 /blog/179642 这样的一个链接，而不包括href=的这样的东西，所以，我们将正则里面匹配 /blog/179642 的部分加上（），系统会自动获取这个()内的内容。注意，（）是必须有的。
	完整的XML文件写法参考WEB-INF/example.xml。
第二步：裁减
	先去掉网页中多余的部分，我们裁减掉头部。具体写法参考xml文件。
第三步：处理
	对一个网页进行处理之前我们需要详细的分析网页的结构，以http://playfish.javaeye.com/blog/179642 这篇文章为例。
	分析处理过程如下：
	我们想要的内容都在一个id为main的div里面。我们先做一下裁减，将无关的内容裁减掉。我们定义flow为true的处理过程，它将返回裁减后的内容。
<process flow="true">
	<tag-filter pos="1" key="id" value="main" />
</process>
	然后我们抓取页面头部的标题作为我们保存到数据库的标题，由于比较复杂，我们使用正则表达式来获得
<process flow="false" table="javaeye" field="title">
	<regex-filter>
		<![CDATA[
			<div class=\"blog_title\">.*<h3><.*?>(.*?)</a>.*?</div>
		]]>
	</regex-filter>
</process>
		我们要获得的部分在只有文字，html标签是不要的。所以，将匹配文字内容的部分我们放在()里面。注意，（）是必须的。
	接下来，获得它的关键字作为文章的tag，我们同样使用正则来完成。
<process flow="false" table="javaeye" field="tags">
	<regex-filter>
			<![CDATA[
				<div class=\"blog_title\">.*?</strong>:(.*?)</div>
			]]>
	</regex-filter>
</process>
	最后，我们获得文章的内容。这里我们用一种更简单的方式，标志位方法。我们发现文章的所有正文内容都在<div class="blog_content">跟<div class="blog_bottom">这2个字符串之间。而这2个字符串又是唯一的，不会重复。对于这样情况使用标志位方法是最好的也是最简单的。记住：如果想要抓取的内容在2个唯一性的字符串中间，那么使用标志位方法是最好最简单的方法。
<process flow="false" table="javaeye" field="content">
	<flag-filter>
		<start-flag>
			<![CDATA[
				<div class="blog_content">
			]]>
		</start-flag>
		<end-flag>
			<![CDATA[
				<div class="blog_bottom">
			]]>
		</end-flag>
	</flag-filter>
</process>
第四步：抓取
	只要执行SystemCore并带上要执行的xml文件名作为参数，任务就会开始。可以同时观察数据库跟控制台来观察抓取的进度。
