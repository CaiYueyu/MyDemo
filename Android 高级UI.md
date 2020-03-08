### 一.AndroidUI绘制流程

#### 1.setCotentView之后做了什么?

1.在phonewindow对象里面的setContentView方法中，调用了installDecor（）方法
2.installDecor方法主要处理两件事，分别是generateDecor()和generateLayout方法
3.generateDecor主要是去初始化一个decorView
4.generateLayout方法主要是根据不同主题，设置不同的feature，加载不同的基础容器，比如R.layout_screen_simple.xml
通过findViewById com.android.internal.R.id.content 找到contentParent，返回contentParent

**视图结构如下图所示**：

![decorView视图结构](https://github.com/CaiYueyu/MyDemo/blob/master/image/decorView%E8%A7%86%E5%9B%BE%E7%BB%93%E6%9E%84.png?raw=true)

**类图结构如下：**

![类图结构](https://github.com/CaiYueyu/MyDemo/blob/master/image/decorView%E7%B1%BB%E5%9B%BE%E7%BB%93%E6%9E%84.png?raw=true)

1.Activity持有抽象window类

2.PhoneWindow类实现抽象window类，PhoneWindow类实现了DecorView类

3.DecorView继承FrameLayout，是所有应用窗口的根View



#### 2.View的绘制入口

1.ActivityThread中的handleResumeActivity方法
	-->WindowManagerImpl.addView(decorView,layoutParams)
	-->WindowManagerGlobal.addView
	
2.View绘制的类及方法
	ViewRootImpl.setView(docorView,layoutParams,parentView)
	-->ViewRootImpl.requestLayout()-->scheduleTraversals()-->doTraversal()
	-->performTraversals()
3.View绘制三大步骤
	ViewRootImpl.performMeasure//测量
	ViewRootImpl.performLayout //布局
	ViewRootImpl.perfromDaw//绘制

#### 3.View的测量

View = 模式 + 尺寸->MeasureSpec 32位int值
specMode + specSize
三种mode
UNSPECIFIED //父容器不对View做任何限制，系统内部使用
EXACTLY //父容器检测出View的大小，View的大小就是SpecSize，LayoutParams 对应match_parent
AT_MOST //父容器指定容器大小，View的大小不能超过这个大小，对应wrap_content

获取顶层view（decorView）的测量规格在viewRootImpl 中的getRootMeasureSpec（）方法中,主要由窗口大小和自身的LayoutParams属性来决定，
遵守如下规则
1.当LayoutParams.MATCH_PARENT : 精确模式，大小为窗口大小
2.当LayoutParams.WRAP_CONTENT : 最大模式，大小最大为窗口大小
3.固定大小 ： 精确模式，大小为LayoutParams大小

View的MeasureSpec由父容器的MeasureSpec和自身的LayoutParams决定
参考ViewGroup的getChildMeasureSpec()方法中。

总结如下表：

![](https://upload-images.jianshu.io/upload_images/944365-76261325e6576361.png?imageMogr2/auto-orient/strip|imageView2/2/w/751/format/webp)

其中的规律总结：（以子View为标准，横向观察）

![](https://upload-images.jianshu.io/upload_images/944365-6088d2d291bbae09.png?imageMogr2/auto-orient/strip|imageView2/2/w/660/format/webp)

**ViewGroup*和View测量过程的区别：****

ViewGroup： measure-->onMeasure(测量子控件的宽高)-->setMeasuredDimension-->setMeasuredDimensionRaw保存自身宽高
View : measure-->onMeasure()-->getDefaultSize-->setMeasuredDimension-->setMeasuredDimensionRaw保存自身宽高

#### 4.View的布局

1.调用view.layout确定自身位置，mtop，mLeft，mRight，mBottom
2.如果是ViewGroup，则需要调用onLayout来确定子view的位置
ViewGroup：layout(确定自己的四个点位置)-->onLayout(进行子View的布局)
View ： layout(确定自己的四个点位置)

#### 5.View的绘制

ViewGrop的绘制 ：
1.绘制背景，drawBackground
2.绘制自己 onDraw
3.绘制子view dispatchDraw
4.绘制前景，滚动条等装饰onDrawForeground

View的绘制 ： 
1.绘制背景，drawBackground
2.绘制自己 onDraw
3.绘制前景，滚动条等装饰onDrawForeground

#### 6.自定义View流程

重写onMeasure -->onLayout(容器)-->onDraw

### 二.Paint画笔高级使用

#### 1.Paint概念

画笔，保存了绘制几何图形，文本和位图的样式和颜色信息

#### 2.常用API

` mPaint = new Paint(); //初始化
  mPaint.setColor(Color.RED);// 设置颜色
  mPaint.setARGB(255, 255, 255, 0); // 设置 Paint对象颜色,范围为0~255
  mPaint.setAlpha(200); // 设置alpha不透明度,范围为0~255
  mPaint.setAntiAlias(true); // 抗锯齿
  mPaint.setStyle(Paint.Style.FILL); //描边效果
  mPaint.setStrokeWidth(4);//描边宽度
  mPaint.setStrokeCap(Paint.Cap.ROUND); //圆角效果
  mPaint.setStrokeJoin(Paint.Join.MITER);//拐角风格
  mPaint.setShader(new SweepGradient(200, 200, Color.BLUE, Color.RED)); //设置环形渲染器
  mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN)); //设置图层混合模式
  mPaint.setColorFilter(new LightingColorFilter(0x00ffff, 0x000000)); //设置颜色过滤器
  mPaint.setFilterBitmap(true); //设置双线性过滤
  mPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.NORMAL));//设置画笔遮罩滤镜 ,传入度数和样式
  mPaint.setTextScaleX(2);// 设置文本缩放倍数
  mPaint.setTextSize(38);// 设置字体大小
  mPaint.setTextAlign(Paint.Align.LEFT);//对其方式
  mPaint.setUnderlineText(true);// 设置下划线
  String str = "Android高级工程师";
  Rect rect = new Rect();
  mPaint.getTextBounds(str, 0, str.length(), rect); //测量文本大小，将文本大小信息存放在rect中
  mPaint.measureText(str); //获取文本的宽
  mPaint.getFontMetrics(); //获取字体度量对象`

#### 3.setShader（）

setShader（Shader shader）

参数着色器对象，一般使用Shader的几个子类

1.LinearGradient ： 线性渲染

2.RadialGradient ： 环形渲染

3.SweepGradient ：扫描渲染

4.BitmapShader：位图渲染

5.ComposeShader：组合渲染，例如LinearGradient  + BitmapShader

##### 1.LinearGradient线性渲染

构造方法：

```
LinearGradient(float x0, float y0, float x1, float y1,
            @ColorInt int color0, @ColorInt int color1,
            @NonNull TileMode tile) 
```

参数：x0，y0，x1，y1 ：渐变的两个端点的位置

color0，color1 是端点的颜色

title ：端点范围之外的着色规则，类型是TileMode

