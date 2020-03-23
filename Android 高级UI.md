

### 一.AndroidUI绘制流程

#### 1.DecorView加载流程

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
EXACTLY //父容器检测出View的大小，View的大小就是SpecSize或者LayoutParams 。对应match_parent
AT_MOST //父容器指定容器大小，View的大小不能超过这个大小，对应wrap_content

获取顶层view（decorView）的测量规格在viewRootImpl 中的getRootMeasureSpec（）方法中。**对于顶层View（DecorView），它的MeasureSpec则是由窗口的尺寸和自身的LayoutParams决定的**
遵守如下规则
1.当LayoutParams.MATCH_PARENT : 精确模式，大小为窗口大小
2.当LayoutParams.WRAP_CONTENT : 最大模式，大小最大为窗口大小
3.固定大小 ： 精确模式，大小为LayoutParams大小

**View的MeasureSpec由父容器的MeasureSpec和自身的LayoutParams决定**

参考ViewGroup的getChildMeasureSpec()方法中。



总结如下表：

![](https://upload-images.jianshu.io/upload_images/944365-76261325e6576361.png?imageMogr2/auto-orient/strip|imageView2/2/w/751/format/webp)

其中的规律总结：（以子View为标准，横向观察）

![](https://upload-images.jianshu.io/upload_images/944365-6088d2d291bbae09.png?imageMogr2/auto-orient/strip|imageView2/2/w/660/format/webp)

**ViewGroup*和View测量过程的区别：****

ViewGroup： measure-->onMeasure(测量子控件的宽高)-->setMeasuredDimension-->setMeasuredDimensionRaw保存自身宽高
View : measure-->onMeasure()-->getDefaultSize-->setMeasuredDimension-->setMeasuredDimensionRaw保存自身宽高



**小贴士**

在Activity的onCreate，onStart,onResume能否获取到View的高宽？

答：不能，因为View的measure和Activity的生命周期不是同步的，因此无法保证在Activity这些生命周期执行的时候，View已经完成测量了。解放方法：

1.在onWindowFocusChanged这个方法去获取，但onWindowFocusChanged这个方法会被频繁调用。2.view.post(runnable) 通过post将一个runnable投递到消息队列的尾部，等待Looper调用runnable的时候，View已经初始化好了

3.使用ViewTreeObserver的OnGlobalLayoutListener，也同样存在调用多次的可能性



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

### 二.Paint Cavans高级绘制

#### 1.Paint画笔高级使用

##### 1.Paint概念

画笔，保存了绘制几何图形，文本和位图的样式和颜色信息

##### 2.常用API

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

##### 3.setShader（）

setShader（Shader shader）

参数着色器对象，一般使用Shader的几个子类

1.LinearGradient ： 线性渲染

2.RadialGradient ： 环形渲染

3.SweepGradient ：扫描渲染

4.BitmapShader：位图渲染

5.ComposeShader：组合渲染，例如LinearGradient  + BitmapShader

**1.LinearGradient线性渲染**

构造方法：

```
LinearGradient(float x0, float y0, float x1, float y1, @NonNull @ColorInt int colors[], @Nullable float positions[], @NonNull TileMode tile)
```

（x0,y0）: 渐变起始点坐标

（x1,y1）：渐变结束点坐标

（color0）：渐变开始点颜色，16进制的颜色表示，必须带有透明度

（color1）：渐变结束点颜色，16进制的颜色表示，必须带有透明度

（colors）：渐变颜色数组，可以填多个颜色值

（positions）：位置数组，position的取值范围[0,1],作用是指定某个位置的颜色值，如果传null，渐变就是线性变化

（tile）：用于指定控件区域大于指定的渐变区域是，空白区域的颜色填充的方式，有如下三种方式：

////               REPEAT, 绘制区域超过渲染区域的部分，重复排版
////               CLAMP， 绘制区域超过渲染区域的部分，会以最后一个像素拉伸排版
////               MIRROR, 绘制区域超过渲染区域的部分，镜像翻转排版

**2.RadialGradient环形渲染**

```
RadialGradient(float centerX, float centerY, float radius, @ColorInt int colors[], @Nullable float stops[], TileMode tileMode)
```

（centerX，centerY）：shader的中心坐标，开始渐变的坐标

（radius）：渐变的半径

（centerColor，edgeColor）：中心点渐变的颜色值，边界渐变的颜色值

（colors）：渐变颜色数组

（stops）：渐变位置数组，类似扫描渐变的positions数组，取值[0,1]，中心点为0，半径到达位置为1.0f

（tileMode）：同线性渲染的tile

**3.SweepGradient扫描渲染**

```
SweepGradient(float cx, float cy, @ColorInt int color0,int color1)
```

（cx，cy）：渐变中心坐标

（color0，color1）：渐变起始和结束的颜色值

（colors，positions）：类似LinearGradient，用于多颜色渐变，positions为null是，根据颜色线性渐变

**4.BitmapShader位图渲染**

```
BitmapShader(@NonNull Bitmap bitmap, @NonNull TileMode tileX, @NonNull TileMode tileY)
```

（bitmap）：构造shader使用的bitmap

（tileX）：X轴方向的TileMode

（tileY）：Y轴方向的TileMode

**5.ComposeShader组合渲染**

```
ComposeShader(@NonNull Shader shaderA, @NonNull Shader shaderB, Xfermode mode)
ComposeShader(@NonNull Shader shaderA, @NonNull Shader shaderB, PorterDuff.Mode mode)
```

（shaderA，shaderB）：要混合的两种shader

（Xfermode mode）：组合两种shader颜色的模式

（PorterDuff.Mode mode）：组合两种shader颜色的模式

##### 4.Paint 颜色相关图层混合模式/滤镜

**1.图层混合模式 PorterDuff.Mode**

概念：将所绘制图形的像素与Canvas中所对应位置的像素按照一定规则混合，形成新的像素值，更新Canvas中最终的像素颜色值。有18种模式

1.需要使用图层混合模式的API

​	a).ComposeShader

​	b).画笔Paint.setXfermode()

​	c).PorterDuffColorFilter（颜色过滤器）

2.画笔Paint.setXfermode() 使用图层混合模式步骤

​	a).禁止硬件加速,因为在Android API 14之后，图层混合有些API不适合硬件加速的，但是android默认是开启硬件加速的，所以需要关闭。

```
        //禁止硬件加速
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
```

​	b).离屏绘制,<u>因为在图层混合过程中，会将背景也考虑进去做混合计算</u>

```
//        //离屏绘制
int layerId = canvas.saveLayer(0,0, getWidth(), getHeight(), mPaint, Canvas.ALL_SAVE_FLAG);
```

​	c).设置图层混合模式，绘制，清除混合模式

```
//        //目标图
        canvas.drawBitmap(createRectBitmap(mWidth, mHeight), 0, 0, mPaint);
//        //设置混合模式
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
//        //源图，重叠区域右下角部分
        canvas.drawBitmap(createCircleBitmap(mWidth, mHeight), 0, 0, mPaint);
//        //清除混合模式
        mPaint.setXfermode(null);
```

​	d).图层恢复

```
        canvas.restoreToCount(layerId);
```

3.离屏绘制

概念：通过使用离屏缓冲，把要绘制的内容单独绘制在缓冲层，保证Xfermode的使用不会出现错误结果

使用离屏缓冲有两种方式：

​	a）Canvas.saveLayer(),可以做短时的离屏缓冲，在绘制前保存，绘制后恢复（重要）

​	b）View.setLayerType(),直接把整个View都绘制在离屏缓冲中

```
        setLayerType(LAYER_TYPE_HARDWARE,null);//使用GPU来缓冲
        setLayerType(LAYER_TYPE_SOFTWARE,null);//使用一个Bitmap来缓冲
```

**2.图层混合的18种模式**

```
//其中Sa全称为Source alpha表示源图的Alpha通道；Sc全称为Source color表示源图的颜色；Da全称为Destination alpha表示目标图的Alpha通道；Dc全称为Destination color表示目标图的颜色，[...,..]前半部分计算的是结果图像的Alpha通道值，“,”后半部分计算的是结果图像的颜色值。
    //效果作用于src源图像区域
    private static final Xfermode[] sModes = {
            //所绘制不会提交到画布上
            new PorterDuffXfermode(PorterDuff.Mode.CLEAR),
            //显示上层绘制的图像
            new PorterDuffXfermode(PorterDuff.Mode.SRC),
            //显示下层绘制图像
            new PorterDuffXfermode(PorterDuff.Mode.DST),
            //正常绘制显示，上下层绘制叠盖
            new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER),

            //上下层都显示，下层居上显示
            new PorterDuffXfermode(PorterDuff.Mode.DST_OVER),
            //取两层绘制交集，显示上层
            new PorterDuffXfermode(PorterDuff.Mode.SRC_IN),
            //取两层绘制交集，显示下层
            new PorterDuffXfermode(PorterDuff.Mode.DST_IN),
            //取上层绘制非交集部分，交集部分变成透明
            new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT),

            //取下层绘制非交集部分，交集部分变成透明
            new PorterDuffXfermode(PorterDuff.Mode.DST_OUT),
            //取上层交集部分与下层非交集部分
            new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP),
            //取下层交集部分与上层非交集部分
            new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP),
            //去除两图层交集部分
            new PorterDuffXfermode(PorterDuff.Mode.XOR),

            //取两图层全部区域，交集部分颜色加深
            new PorterDuffXfermode(PorterDuff.Mode.DARKEN),
            //取两图层全部区域，交集部分颜色点亮
            new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN),
            //取两图层交集部分，颜色叠加
            new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY),
            //取两图层全部区域，交集部分滤色
            new PorterDuffXfermode(PorterDuff.Mode.SCREEN),

            //取两图层全部区域，交集部分饱和度相加
            new PorterDuffXfermode(PorterDuff.Mode.ADD),
            //取两图层全部区域，交集部分叠加
            new PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
    };
```

**3.滤镜效果 LightColorFilter**

1.作用：可以模仿光照效果

2.构造方法：public LightingColorFilter(@ColorInt int mul, @ColorInt int add)

   参数mul和add都是和颜色值格式相同的int值，其中mul用来和目标像素相乘，add用来和目标像素相加：

如：R' = R *mul.R/0xff + add.R;  G' = G * mul.G + add.G;  B' = B * mul.B/0xff + add.B

3.使用方法

```
LightingColorFilter lighting = new LightingColorFilter(0x00ffff,0x000000)
paint.setColorFilter(lighting)
```

**4.PorterDuffColorFilter滤镜（图层混合）**

1.作用 ：根据构造方法传入的color创建一个新的图层，再根据混合模式进行图层混合

2.构造方法 ：public PorterDuffColorFilter(@ColorInt int color, @NonNull PorterDuff.Mode mode)

​	参数：

​	color ： 具体的颜色值，例如Color.RED

​	mode : 指定PorterDuff.Mode 混合模式

3.使用方法

```
PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(Color.RED,PorterDuff.Mode.DARKEN);
paint.setColorFilter(porterDuffColorFilter);
```

**5.ColorMatrixColorFilter滤镜（颜色数组）**

1.作用：通过传入色彩矩阵来处理图像色彩效果

2.构造方法

```
    public ColorMatrixColorFilter(@NonNull float[] colorMatrix)
```

参数 ： colorMatrix矩阵数组

3.使用方法

```
float[] colorMatrix = {
                2,0,0,0,0,   //red
                0,1,0,0,0,   //green
                0,0,1,0,0,   //blue
                0,0,0,1,0    //alpha
        };

        ColorMatrix cm = new ColorMatrix();
//        //亮度调节
//        cm.setScale(1,2,1,1);

//        //饱和度调节0-无色彩， 1- 默认效果， >1饱和度加强
//        cm.setSaturation(2);

        //色调调节
        cm.setRotate(0, 45);

        mColorMatrixColorFilter = new ColorMatrixColorFilter(cm);
        mPaint.setColorFilter(mColorMatrixColorFilter);
```

4.关于色彩矩阵

在Android中，系统使用一个颜色矩阵--ColorMatrix来处理图像的色彩效果，对于图像的每个像素点，都有一个颜色分量矩阵用来保存颜色的RGBA值（如下图矩阵C），Android中的颜色矩阵是一个4 * 5的数字矩阵，它用来对图片的色彩进行处理（如矩阵A）：
$$
A=\left[
\matrix{
	a&b&c&d&e\\
	f&g&h&i&j\\
	k&l&m&n&o\\
	p&q&r&s&t
}
\right]
C=\left[
\matrix{
	R\\
	G\\
	B\\
	A\\
	1
}
\right]
$$
如果我们想要改变一张图像的色彩显示效果，我们会用矩阵的乘法运算来修改颜色分量矩阵的值，在Android中，它会以一个一位数组的形式来存储[a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t],而C是一个颜色矩阵分量，在处理图像时，使用矩阵乘法运算AC来处理颜色分量矩阵，如下：
$$
R=AC=\left[
\matrix{
	a&b&c&d&e\\
	f&g&h&i&j\\
	k&l&m&n&o\\
	p&q&r&s&t
}
\right]
\left[
\matrix{
	R\\
	G\\
	B\\
	A\\
	1
}
\right]
=\left[
\matrix{
	aR&bG&cB&dA&e\\
	fR&gG&hB&iA&j\\
	kR&lG&mB&nA&o\\
	pR&qG&rB&sA&t
}
\right]
=\left[
\matrix{
	R1\\
	G1\\
	B1\\
	A1
}
\right]
$$
利用线性代数知识可知：

```
R1 = aR + bG + cB + dA + e;
G1 = fR + gG + hB + iA + j;
B1 = kR + lG + mB + nA + o;
A1 = pR + qG + rB + sA + t;
```

从这个公式可以发现，矩阵A中

```
1.第一行的abcde用来决定新的颜色中的R-------红色
2.第二行的fghij用来决定新的颜色中的G-------绿色
3.第三行的klmno用来决定新的颜色中的B-------绿色
4.第四行的pqrst用来决定新的颜色中的A-------透明度
5.矩阵中的第五列---ejot的值分别用来决定每个分量的offset，即偏移量
```

**初始颜色矩阵**

我们重新看一下矩阵变换公式，以R分量为例，

```
R1 = aR + bG + cB + dA + e
```

令a=1，b，c，d，e都为0，则有R1 = R，同理对第二，三，四行进行操作，可以构造出一个矩阵，如下：
$$
A=\left[\matrix{
	1&0&0&0&0\\
	0&1&0&0&0\\
	0&0&1&0&0\\
	0&0&0&1&0
}
\right]
$$
把该矩阵带入公式R=AC公式中，可得R1=R，G1=G，B1=B，A1=A，不会对原有颜色进行任何修改，则该矩阵称为初始颜色矩阵。

**如果修改颜色值，通常有两种方法**

1.改变颜色的offset（偏移量）的值；

2.改变对应的RGBA值。

#### 2.Canvas详解

##### 1.概念

画布，通过画笔绘制几何图形，文本，路径和位图等

##### 2.常用API

常用API分为绘制图形，位置变换，状态保存和回复

```
/* 绘制几何图形，文本，位图等常用API */
//在指定坐标绘制位图
void drawBitmap(@NonNull Bitmap bitmap, float left, float top, @Nullable Paint paint) 
//根据给定的起始点和结束点直接绘制连线
void drawLine(float startX, float startY, float stopX, float stopY,
            @NonNull Paint paint)
//根据指定的path，绘制连线
void drawPath(@NonNull Path path, @NonNull Paint paint) 
//根据给定的坐标，绘制点
drawPoint(float x, float y, @NonNull Paint paint)
//根据给定的坐标，绘制文本
void drawText(@NonNull String text, float x, float y, @NonNull Paint paint)

/* 位置，形状变换等常用API */
//平移操作
translate(float dx, float dy) 
//缩放操作
scale(float sx, float sy)
//旋转操作
rotate(float degrees)
//倾斜操作
skew(float sx, float sy)
//切割操作，参数指定区域内可以继续绘制
clipXXX（....）
//反向切割操作，参数指定区域内不可绘制
clipOut(....)
//通过matrix实现平移，缩放，旋转等操作
setMatrix(@Nullable Matrix matrix) 
```

##### 3.状态保存和回复

作用：Canvas调用了translate，scale，rotate，skew，clipRect等变换后，后续的操作都是基于变换后的Canvas，都会受到影响，对于后续的操作很不方便，所以Canvas提供了save，saveLayer，saveLayerAlpha，restore，restoreToCount来保存和恢复状态。

```
saveLayer则是通过创建一个固定大小的图层，保存该图层，后续的绘制基于这个图层之上
```

#### 3.Path详解

##### 1.概念：

路径，可用于绘制直线，曲线构成几何路径，，还可以根据路径绘制文字。

##### 2.常用API

常用的API如移动，连线，闭合，添加图形等

```
mPath.moveTo(100, 70); //移动坐标
//一阶贝塞尔曲线，表示的是一条直线
mPath.lineTo(140, 800);//连线    
mPath.rLineTo(40,730);   //等同于上一行代码效果
mPath.close();//设置曲线是否闭合
        
//添加子图形addXXX
mPath.addArc(200, 200, 400, 400, -225, 225);        //添加弧形

//Path.Direction.CW表示顺时针方向绘制，CCW表示逆时针方向
mPath.addRect(500, 500, 900,900, Path.Direction.CW);
//添加一个圆
mPath.addCircle(700, 700, 200, Path.Direction.CW);
//添加一个椭圆
mPath.addOval(0,0,500,300, Path.Direction.CCW);

//              //追加图形
//        //xxxTo画线
//        mPath.arcTo(400, 200, 600, 400, -180, 225, false);
//
//        //forceMoveTo，true，绘制时移动起点，false，绘制时连接最后一个点与圆弧起点
//        mPath.moveTo(0, 0);
//        mPath.lineTo(100, 100);
//        mPath.arcTo(400, 200, 600, 400, 0, 270, false);

//              //添加一个路径
//        mPath.moveTo(100, 70);
//        mPath.lineTo(140, 180);
//        mPath.lineTo(250, 330);
//        mPath.lineTo(400, 630);
//        mPath.lineTo(100, 830);
//
//        Path newPath = new Path();
//        newPath.moveTo(100, 1000);
//        newPath.lineTo(600, 1300);
//        newPath.lineTo(400, 1700);
//        mPath.addPath(newPath);

//              //添加圆角矩形， CW顺时针，CCW逆时针
//        RectF rectF5 = new RectF(200, 800, 700, 1200);
//        mPath.addRoundRect(rectF5, 20, 20, Path.Direction.CCW);
//
//        //画二阶贝塞尔曲线
//        mPath.moveTo(300, 500);
////        mPath.quadTo(500, 100, 800, 500);
//        //参数表示相对位置，等同于上面一行代码
//        参数相当于上面一行代码的x，y参数减去moveTo方法的x，y参数
//        mPath.rQuadTo(200, -400, 500, 0);

        //画三阶贝塞尔曲线
        mPath.moveTo(300, 500);
//        mPath.cubicTo(500, 100, 600, 1200, 800, 500);
        //参数表示相对位置，等同于上面一行代码，
        //参数相当于上面一行代码的x，y参数减去moveTo方法的x，y参数
        mPath.rCubicTo(200, -400, 300, 700, 500, 0);
```

##### 3.贝塞尔曲线

贝塞尔曲线是用一系列点来控制曲线状态的，我们将这些点分为两类，一类是数据点，一类是控制点

贝塞尔曲线分为多阶：

一阶：没有控制点，仅有两个数据点

二阶：两个数据点，一个控制点

三阶：两个数据点，两个控制点

关于贝塞尔曲线，更多参考资料可以参考该博文：

[自带美感的贝塞尔曲线原理与实战](https://juejin.im/post/5c3988516fb9a049d1325c83)

#### 4.PathMeasure详解

##### 1.概念

路径测量，一个用来测量Path的工具类

Path的绘制有很多种方法，例如Android API，Bezier曲线或者数学函数表达式等，而高级的动画都会要求这个Path的坐标点是可控的，这样才能更好地扩展基于Path的动画。而如何确定Path点的坐标，这就用到了工具类PathMeasure。

##### 2.常用的API

常用API如Path长度测量，Path跳转，Path片段获取等

```
PathMeasure pathMeasure = new PathMeasure() //创建一个PathMeasure对象
pathMeasure.setPath(path,true) //设置关联Path
PathMeasure (Path path, boolean forceClosed) //在构造方法里关联Path
gentLength() //获取计算的长度
getSegment(float startD, float stopD, Path dst, boolean startWithMoveTo) //获取路径的片段，前两个参数表示起止点坐标，dst表示截取path输出结果，startWithMoveTo表示是否从上一次截取的终点处开始截取
pathMeasure.nextContour();//跳转到下一条曲线，跳转成功返回true，失败返回false

getPosTan(float distance, float[] pos, float[] tan) //获取某点坐标及其切线坐标
//        pathMeasure.getPosTan(pathMeasure.getLength() * mFloat,pos,tan);
//        Log.e("TAG", "onDraw: pos[0]="+pos[0]+";pos[1]="+pos[1]);
//        Log.e("TAG", "onDraw: tan[0]="+tan[0]+";tan[1]="+tan[1]);
//
//        //计算出当前的切线与x轴夹角的度数，***重要***
//        double degrees = Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI;

boolean getMatrix(float distance, Matrix matrix, int flags) //distance表示距离path起点的长度，范围是0到path的长度，matrix会将信息存放在matrix里面，flags指定存放信息的类型，它的值有两个：
POSISTION_MATRIX_FLAG //位置信息
TANGENT_MATRIX_FLAG //当前点在曲线上的方向，对应getPostTan上float[] tan数据
实例 ：pathMeasure.getMatrix(pathMeasure.getLength() * mFloat, mMatrix, PathMeasure.POSITION_MATRIX_FLAG | PathMeasure.TANGENT_MATRIX_FLAG);
```

**forceClosed参数对绑定的Path不会产生任何影响，只会对PathMeasure 的测量结果有影响。当forceClosed为true，在测量path长度时，会自动补上使其闭合，长度就为闭合的长度。但是forceClosed无论true还是false，都不影响Path本身的值。**

**startWithMoveTo为true时，表示截取一部分存入dst中，并且使用moveTo保持截取得到的Path第一个点位置不变。**

**getMarix相对于getPostTan方法在使用上更简单，因为把位置和角度都保存在marix对象中**



关于更多的PathMeasure，请参考 ：[Android动画——PathMeasure](https://www.jianshu.com/p/2b9055c2ee31)

### 三.事件传递机制

#### 1.事件的定义：

当用户触摸屏幕时，将产生触摸行为（Touch事件）。

事件的类型有四种：

ACTION_DOWN : 手指刚接触屏幕

ACTION_UP : 手指从屏幕上松开

ACTION_MOVE : 手指在屏幕上滑动

ACTION_CANCEL :非人为因素取消

#### 2.事件序列：

1.点击屏幕后松开，事件序列为DOWN->UP

2.点击屏幕后滑动一会在松开，事件序列为：DOWN->MOVE->....MOVE->UP

![](.\image\事件序列.png)

#### 3.事件分发对象

1.Activity ：控制生命周期&处理事件

2.ViewGroup ：一组View的集合（含多个子View）

3.View ： 所有的UI组件类

#### 4.事件分发的主要方法：

1.dispatchTouchEvent(MotionEvent ev) : 用来进行事件分发

2.onInterceptTouchEvent(MotionEvent ev) : 判断是否拦截事件（该方法只存在ViewGroup中）

3.onTouchEvent（MotionEvent ev）：消费事件

#### 5.ViewGroup事件分发

如下伪代码表示：

```
private TouchTarget mFirstTouchTarget; //记录第一个能够消费的子View，这是个一个链表，保存能够接收事件进行消费的子View
public boolean dispatchTouchEvent(MotionEvent ev){
	boolean handled = false;
	final boolean intercept;
	if(ACTION_DOWN || ！mFirstTouchTarget){
		//如果是down事件或者存在可以消费这个事件的子View
		final boolean disallowIntercept = (mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0;
		if(!disallowIntercept){
			如果子View没有禁止父View拦截事件，是否拦截取决于该ViewGroup是否拦截事件
			intercepted = onInterceptTouchEvent(ev);
		}else{
			//子View禁止了父View拦截
			intercepted = false;
		}
	}else{
		//不存在可以消费事件的子View，并且不是ACTION_DOWN事件，默认拦截
		intercepted = true;
	}
	if(!intercepted){   //父View不拦截事件的情况
		 for (int i = childrenCount - 1; i >= 0; i--) {
		 	//遍历所有的子View
		 	 final int childIndex = getAndVerifyPreorderedIndex(childrenCount,
             						i, customOrder); //如果有两个子View相交，取上层View
             final View child = getAndVerifyPreorderedView(
                                    preorderedList, children, childIndex);
             
             if(!canViewReceivePointerEvents(child) || !isTransformedTouchPointInView){
             	//如果子View不再触摸区域，或者子View正在执行动画，那么跳过
             	continue；
             }    
             newTouchTarget = getTouchTarget(child); //从touchTarget链表查找是否有保存
             if(dispatchTransformedTouchEvent(ev, false, child, idBitsToAssign)) {
             	//调用dispatchTransformedTouchEvent()方法，递归遍历child的子View，如果能找到消费的子View，则把Child保存到可消费链表touchTarget里面,跳出循环
                newTouchTarget = addTouchTarget(child, idBitsToAssign);
                alreadyDispatchedToNewTouchTarget = true;  
                break;
             }		 	
	}
	if(!mFirstTouchTarget){
        //找不到可消费事件的子View
        handled = dispatchTransformedTouchEvent(ev, canceled, null,
        TouchTarget.ALL_POINTER_IDS);		 	
	 }else{
         //这里主要处理MOVE和UP事件，遍历touchTarget链表 查找可消费事件的View，进行事件传递
         TouchTarget target = mFirstTouchTarget;
         while (target != null){
             final TouchTarget next = target.next;
             if (alreadyDispatchedToNewTouchTarget && target == newTouchTarget) {
             	handled = true;//如果之前已经传递过了，则表示消费了，事件将停止传递
         	}else{
         		if (dispatchTransformedTouchEvent(ev, cancelChild,
         				target.child, target.pointerIdBits)) {
         			handled = true;
         			//事件进行向下传递，如果返回true，则消费
         		}                	
         	}            	
        	 target = next;
         } 		 	
      }
	}
	return handled；
}
```

dispatchTransformedTouchEvent()方法

```
private boolean dispatchTransformedTouchEvent(MotinEvent event,boolean cancel,View child,int desiredPointerIdBits){
	final boolean handled;
	...
	if(child == null){
		handled = super.dispatchTouchEvent(event);//找不到子View，调用View的dispatchTouchEvent(event)
	}else{
		handled = child.dispatchTouchEvent(event);//调用View的dispatchTouchEvent(event)
	}
	event.setAction(oldAction);
	return handled;
}
```

ViewGroup的拦截事件，默认返回false

```
public boolean onInterceptTouchEvent(MotionEvent ev){return false}
```

#### 6.View的事件分发

```
public boolean dispatchTouchEvent(MotionEvent event) {
	
    boolean result = false;
    //当前View是否可见（未被其他窗口遮盖着且未隐藏）
    if (onFilterTouchEventForSecurity(event)) {
        ListenerInfo li = mListenerInfo;
        if (li != null && li.mOnTouchListener != null
         && (mViewFlags & ENABLED_MASK) == ENABLED
            && li.mOnTouchListener.onTouch(this, event)) {
            //如果设置了TouchListener，先响应TouchListener.onTouch
            result = true;
        }

	if (!result && onTouchEvent(event)) {
		//如果TouchListener.onTouch返回了false才执行onTouchEvent
		result = true;
	}
}
```

#### 7.View的事件消费

```
public boolean onTouchEvent(MotionEvent event){
	...
    final boolean clickable = ((viewFlags & CLICKABLE) == CLICKABLE
    	|| (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE)
    	|| (viewFlags & CONTEXT_CLICKABLE) == CONTEXT_CLICKABLE;
	//如果当前View是DISABLED状态且是可点击/可长按则会消费调事件，不让它继续传递
	if ((viewFlags & ENABLED_MASK) == DISABLED) {
		if (action == MotionEvent.ACTION_UP && (mPrivateFlags & PFLAG_PRESSED) != 0) {
			setPressed(false);
		}
		mPrivateFlags3 &= ~PFLAG3_FINGER_DOWN;
		// A disabled view that is clickable still consumes the touch
		// events, it just doesn't respond to them.
		return clickable;
	}
	//如果设置了onTouchDelegate，则会将事件交给代理者处理，直接return true
    if (mTouchDelegate != null) {
    	if (mTouchDelegate.onTouchEvent(event)) {
    		return true;
    	}
    }
    if (clickable || (viewFlags & TOOLTIP) == TOOLTIP) {
    	switch (action) {
    		case MotionEvent.ACTION_UP://抬起，判断是否处理点击事件
    		break;
    		case MotionEvent.ACTION_DOWN://按下，处理长按事件
    		break;
    		case MotionEvent.ACTION_MOVE://移动，检测触摸是否划出了控件，移除响应事件
    		break;
    	}
    	return true;
    }
    return false;
}
```

#### 8.View事件分发整体流程

![](.\image\事件分发模型.png)

#### 9.事件分发结论

1.一个事件序列从手指触摸到屏幕到手指离开屏幕，以DOWN开始，经过不定数的MOVE，以UP结束

2.正常情况下，**一个事件序列**只能被一个View拦截并且消费

3.某个View一旦决定拦截，那么这个事件序列将由它的onTouchEvent处理，并且它的onInterceptTouchEvent不会再调用。

4.**某个View一旦开始处理事件，如果它不消费ACTION_DOWN事件（onTouchEvent返回false），那么同一事件序列中的其他事件都不会再交给它处理**，并且重新交由它的父元素处理（负元素onTouchEvent被调用）

5.事件传递过程由外向内，先给父元素，再由父元素分发给子View，通过requestDisallowInterceptTouchEvent方法可以在子View中干预父元素的事件分发过程，**但ACTION_DOWN除外**。

6.ViewGroup默认不拦截任何事件，onInterceptTouchEvent默认返回false。View没有onInterceptTouchEvent方法，一旦有点击事件传递给它，那么它的onTouchEvent方法就会被调用

 7.View的onTouchEvent默认会消耗事件（返回true）**，除非它是不可点击的（clickable和longClickable同时为false）**，View的longClickable默认是false，clickable则分情况，比如Button默认就是true，TextView默认为false

8.View的enable属性不会影响onTouchEvent的默认返回值，只要它的clickable或者longClickable有一个是true，那么就会消费（onTouchEvent返回true）

9.View响应onClick前提是可点击的，并且收到ACTION_DOWN和ACTION_UP的事件，并且受长按事件影响，当长按事件返回true，onClick不会响应

10.onLongClick在ACTION_DOWN里面判断是否进行响应，想要执行长按事件该View必须是longClickable并且设置了OnLongClickListener

11.**View的onTouch优先级比onClick优先级高**，如果onTouch返回true，则onClick不再调用。只有ACTION_UP才会产生onClick事件，View只要设置了onClick监听，表示消费了事件



### 四.属性动画

#### 1.动画本质：

动画实质上就是改变View在某一个时间点的样式属性，比如通过一个线程每隔一段事件，通过设置View.setX(index++)的值，也能产生动画，这就是属性动画的原理，属性动画实际上就是通过调用View里面的方法，属性动画做了一层封装。

当然，属性动画的作用对象是任意JAVA对象，不仅仅局限于View对象。

具体的工作原理逻辑如下：

![](.\image\属性动画的工作原理.webp)

#### 2.两种属性动画类

1.`ValueAnimator` 是属性动画机制中 最核心的一个类，通过不断控制值的变化，**再不断 手动 赋给对象的属性，从而实现动画效果**，如下图：

![](.\image\属性动画的工作原理.webp)

2.ObjectAnimator类，直接对对象的属性值进行改变操作，从而实现动画效果

```
1.如直接改变 View的 alpha 属性 从而实现透明度的动画效果
2.继承自ValueAnimator类，即底层的动画实现机制是基于ValueAnimator类
```

本质原理： 通过不断控制 值 的变化，再不断 **自动** 赋给对象的属性，从而实现动画效果，

![](.\image\ObjectAnimator类.webp)

#### 3.关键帧



一个动画在特定的时间点上的状态。为什么要将动画分解成不同的关键帧？ 原因是动画需要时间开销才能完成的，如果不给出关键动画，动画的过程将无法控制。在不同的时间点，控件的状态也不一样的。

动画架构分析图如下

![](.\image\动画架构分析图.png)

由上图可知，由于关键帧直接是有时间间隔的，所以需要在两个关键帧之间去实现补帧，这就需要用到**估值器TypeEvaluator和插值器TimeInterpolator **

#### 4.估值器和插值器

1.**TimeInterpolator（时间插值器）：**

根据时间流逝的百分比计算出当前属性值改变的百分比。**简单来说就是改变对象属性的速度快慢**，比如View缩放越来越快，或者越来越慢。

2.**TypeEvaluator（类型估值算法，即估值器）：**

根据当前**属性改变的百分比**来计算改变后的属性值。估值器一般就一个重要方法，就是根据百分比来计算改变后的属性值，如IntEvaluator所示

```
   //参数：fraction ->百分比，这个参数非常重要
   //开始值
   //结束值，
   public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
        int startInt = startValue;
        return (int)(startInt + fraction * (endValue - startInt));
    }
```

对于 **插值器和估值器** 来说，除了系统提供的外，我们还可以**自定义**。实现方式也很简单，因为插值器和估值器都是一个接口，且内部都只有一个方法，我们只要实现接口就可以了。

### 五.屏幕适配

#### 1.限定符适配（通过加载不同的资源文件）

优势：

1.使用简单，无需开发者手动指定

2.google推荐，由系统自己判断

3.适配通过不同的xml布局完成，无需要代码中额外再写

劣势：

1.增加apk大小，xml越来越多

2.不能适配奇葩机型，比如手表

#### 2.百分比适配

需要导入官方包：com.android.support:percent : 28.0.0

app:layout_widthPercent = "50%"

优势：

1.通过百分比定义高宽，比较方便

2.抛弃px dp单位，通过百分比实现，可以在布局完成适配

3.对开发者工作量小

略势：

1.五大布局不能直接使用，所有自定义的布局也必须继承PercentXXXLayout



**自定义百分比**

```
1.自定义一个父容器继承ViewGroup，如
public class RelativePercentLayout extends RelativeLayout
2.重写一个LayoutParams继承自RelativeLayout.LayoutParams
3.在LayoutParams构造方法中，通过
 TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.PercentLayout);
 去获取xml文件中的自定义属性，进行解析
4.重写LayoutParams generateLayoutParams(AttributeSet attrs)，返回第二步重写的LayoutParams
5.在onMeasure方法中去获取父容器的宽高
6.遍历子View，通过设置子View的LayoutParams进行宽高，margin的百分比缩放
```



#### 3.代码动态适配

优势：等比缩放，UI效果一致性

略势：代码侵入性太高

方法：主要就是通过创建工具类，通过获取屏幕的宽高/主流的分辨率手机的宽高，获取一个缩放比例系数，在创建View的时候，通过调用工具类方法，进行缩放。

#### 4.屏幕适配常见方式

1.布局适配

-避免写死控件尺寸，使用wrap_content，match_parent

-设置权重 LinearLayout xxx :layout_weight = "0.5"

-RelativeLayout xxx:layout_centerInParent="true"

-ContraintLayout(约束布局)

​	xxxx : layout_constraintLeft_toLeftOf = "parent"

-Percent-support-lib xxx : layout_widthPercent = "30%"

2.图片资源适配

-.9图或者SVG图实现缩放

-备用位图匹配不同分辨率（不同分辨率res目录下放置不同图片）

3.用户流程适配（比如手机和平板，显示内容不一样等）

-根据业务逻辑执行不同的跳转

-根据别名展示不同的界面

4.限定符适配

-分辨率限定符：drawable-hdpi,drawable-xdpi

-尺寸限定符：layout-small,layout-large

-最小宽度限定符：value-sw360dp,value-sw384dp...

-屏幕方向限定符：layout-land，layout-port

5刘海屏适配

-Android 9.0官方适配

-华为，vivo，oppo

#### 5.修改像素密度

通过修改density,scaleDensity,densityDpi值，直接更改系统内部对于目标尺寸而言的像素密度

1.density ： 表示屏幕的像素密度 = 像素点数/屏幕

2.scaleDensity ：字体缩放比例，默认等于density

3.densityDpi ：屏幕每一英寸上有多少像素点

```
private static final float  WIDTH = 320;//参考设备的宽，单位是dp 320 / 2 = 160
private static float appDensity;//表示屏幕密度
private static float appScaleDensity; //字体缩放比例，默认appDensity
//获取当前app的屏幕显示信息
DisplayMetrics displayMetrics = application.getResources().getDisplayMetrics();

//计算目标值density, scaleDensity, densityDpi
float targetDensity = displayMetrics.widthPixels / WIDTH; // 1080 / 360 = 3.0
float targetScaleDensity = targetDensity * (appScaleDensity / appDensity);
int targetDensityDpi = (int) (targetDensity * 160);
//替换Activity的density, scaleDensity, densityDpi
DisplayMetrics dm = activity.getResources().getDisplayMetrics();
dm.density = targetDensity;
dm.scaledDensity = targetScaleDensity; //该方法可修改字体不随系统改变，如果要修改成字体随系统修改，可监听onCongigurationChanged，修改scaledDensity
dm.densityDpi = targetDensityDpi;
```

**小贴士**

问 ：android如何将PT,DP,SP转成PX？

答：查看TypedValue里面的applyDimension()方法

#### 6.刘海屏适配

**Android 官方9.0刘海屏适配策略**

--如果非全屏模式（有状态栏），app不受刘海屏影响，刘海屏的高度就是状态栏高度

--如果全屏模式，app未适配刘海屏，系统会对界面做特殊处理，竖屏向下移动，横屏向右移动

--适配刘海屏步骤：

1.判断手机厂商

2.判断手机是否有刘海屏

3.设置是否让内容区域延伸进刘海

4.设置空间是否避开刘海区域

5.获取刘海的高度

```
//1.设置全屏
requestWindowFeature(Window.FEATURE_NO_TITLE);
Window window = getWindow();
window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
//2.让内容区域延伸进刘海
WindowManager.LayoutParams params = window.getAttributes();
/**
*  * @see #LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT 全屏模式，内容下移，非全屏不受影响
*  * @see #LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES 允许内容去延伸进刘海区
*  * @see #LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER 不允许内容延伸进刘海区
*/
params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
window.setAttributes(params);
//3.设置成沉浸式
int flags = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
int visibility = window.getDecorView().getSystemUiVisibility();
visibility |= flags; //追加沉浸式
window.getDecorView().setSystemUiVisibility(visibility);
```

判断手机是否有刘海屏

```
DisplayCutout displayCutout;
View rootView = window.getDecorView();
WindowInsets insets = rootView.getRootWindowInsets();
if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && insets != null){
    displayCutout = insets.getDisplayCutout();
     if(displayCutout != null){
         if(displayCutout.getBoundingRects() != null && displayCutout.getBoundingRects().size() > 0 && displayCutout.getSafeInsetTop() > 0){
              return true;
          }
     }
}
```

获取刘海屏高度，默认和状态栏高度一样

```
int resID = getResources().getIdentifier("status_bar_height", "dimen", "android");
	if (resID > 0){
		return getResources().getDimensionPixelSize(resID);
	}
return 0;
或者通过
displayCutout.getSafeInsetTop() 
```

**小贴士**

获取View的LayoutParams需要强转成父容器的LayoutParams类型，因为不同的布局方式的LayoutParams不一样，View的LayoutParams等同于告诉父容器想要怎么布局

### 六.Material Design

--是将经典的设计原则和科技、创新相结合的设计语言

--是一个能在不同平台，不同设备上提供一致的体验的底层系统，代表了一种设计规范

[Material Design中文网站](https://www.mdui.org/design/)

使用：

#### **1 Material Design主题**

**API需要21以上**

--android:style/Theme.Material

--android:style/Theme.Material.Light

--android:style/Theme.Material.DarkActionBar

**API 21以下，使用兼容主题**

--Theme.AppCompat.Light

--Theme.AppCompat.Light.DarkActionBar

**主题常用的属性**

-colorPrimary ： 标题栏的颜色

-colorPrimaryDark ：状态栏颜色

-colorAccent ：强调色

-textColorPrimary ：标题栏上字体颜色

-windowBackgroud ：窗口背景色

-navigationBarColor ： 虚拟导航栏背景颜色

**常用控件**

-ToolBar ： 代替ActionBar，高度可定制性

-DrawerLayout  ： 左拉右拉菜单，类似抽屉功能

-NavigationView/BottomNavigationView ：NavigationView通常和DrawerLayout  一起使用作为侧滑菜单 ，BottomNavigationView 主要用于实现底部导航栏功能

-FloatingActionButton  ：浮动按钮

-Snackbar ： 提示功能

-CardView ：继承FrameLayout，可设置圆角，阴影，也可以包含其他的布局容器和控件

-**CoordinatorLayout** ： 继承ViewGroup，使用类似FrameLayout，有层次结构，后面的布局会覆盖前面布局，通过子View指定behavior，自定义交互行为

-AppBarLayout ： 垂着线性布局，已经响应了CoordinatorLayout 的behavior属性，一般结合CoordinatorLayout 一起使用

-CollapsingToolBarLayout ：折叠的toolBar，一般和CoordinatorLayout 一起使用

-NestedScrollView ：支持嵌套滑动的scrollView

**常用动画**

-Fade淡入

-Slide滑动

-Explode分解

-共享元素

#### 2.CoordinatorLayout相关

**1.CoordinatorLayout**

CoordinatorLayout是用来协调其子view们之间动作的一个父view

--CoordinatorLayout须作为顶层父View

--子View想要与CoordinatorLayout实现"联动性"效果的首要条件是这个View必须实现了NestedScrollingChild接口 ，如RecyclerView，NestedScrollView等新的控件都实现这个接口

--**只有CoordinatorLayout的直接子View才会有效果,子View的子View无效**

**2.Behavior**

Behavior用于当前控件的父控件CoordinatorLayout中的其他子控件的关系

--使用Behavior的控件必须是直接从属于CoordinatorLayout,否者无效

--自定义Behavior的时候**必须覆盖它的两参构造方法**,因为通过反射实例化的时候用的就是该构造方法.

**3.AppBarLayout**

作为一个容器把AppBarLayout内的所有子View"合并"成一个整体的View.

AppBarLayout下的View想实现联动效果必须设置该属性,其中主要的参数包括下面几个:

--scroll - 想滚动就必须设置这个。

--EnterAlways - 当依赖的View向下滑动时,设置该属性的View以相同的方向滑动直到消失

--exitUntilCollapsed - 配合minHeight使用.假设view的Height是300px,minHeight是200px,控件展开的话高度是300px,收缩了后最小高度会停留在200px而不是完全隐藏

--enterAlwaysCollapsed：假设view的Height是300px,minHeight是200px,这时候依赖控件向下滑动,view会随着依赖view慢慢出现,直到view显示了200px后停止滑动.直到依赖控件滑到了顶部再次往下滑动剩下的100px才会出现.(有点类似下拉刷新,必须滑到第一个item再下拉才能刷新)

**4.TabLayout**

作用就是在Toolbar下面显示多个Tab.

就是先对ViewPager进行适配,然后实例化TabLayout,然后调用tablayout.setupWithViewPager(viewPager)让tablayout和Viewpager进行关联,这样滑动viewpager,tablayout的指示器就会跟着改变,点击tablayout的指示器,Viewpager也会切换.



#### 3.沉浸式

**android 4.4版本沉浸式可通过如下两种方式**

1.可通过设置AppTheme，添加一个属性

```
<item name="android:windowTranslucentStatus">true</item>
<item name="android:windowTranslucentNavigation">true</item>
```

2.代码设置

```
getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
```

**android 5.0或者以上版本沉浸式设置**

1.获取window，清除状态栏透明标志位

2.添加window标志位FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS

3.window.setStatusBarColor(Color.TRANSPARENT); 设置状态栏透明

```
Window window = getWindow();
window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//设置状态栏颜色透明
window.setStatusBarColor(Color.TRANSPARENT);

int visibility = window.getDecorView().getSystemUiVisibility();
//布局内容全屏展示
visibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//隐藏虚拟导航栏
visibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//防止内容区域大小发生变化
visibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

window.getDecorView().setSystemUiVisibility(visibility);
```

**小贴士**

如果有toolbar，上面两种方式都会有toolbar被状态栏盖住的问题，这个时候则需要获取状态栏的高度，将toolbar向下移动状态栏的高度

```
ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
layoutParams.height += getStatusBarHeight(context);
view.setPadding(view.getPaddingLeft(), view.getPaddingTop() + getStatusBarHeight(context), view.getPaddingRight(), view.getPaddingBottom());
```

#### 4.CardView

CardView的属性：

第二第三个属性一般会设置，考虑系统兼容性

```
    <!--app:cardBackgroundColor="@color/colorPrimary"  设置cardView背景色 -->
    <!--app:cardPreventCornerOverlap="false" 取消Lollipop以下版本的padding -->
    <!--app:cardUseCompatPadding="true" 为 Lollipop 及其以上版本增加一个阴影padding内边距-->
    <!--app:cardCornerRadius="8dp" 设置cardView圆角效果-->
    <!--app:cardElevation="10dp" 设置cardView Z轴阴影大小-->
    <!--app:cardMaxElevation="6dp" 设置cardView Z轴最大阴影-->
    <!--app:contentPadding="10dp" 设置内容的内边距-->
    <!--app:contentPaddingBottom="12dp" 设置内容的底部内边距-->
    <!--app:contentPaddingLeft="12dp" 设置内容的左边内边距-->
    <!--app:contentPaddingRight="12dp" 设置内容的右边内边距-->
    <!--app:contentPaddingTop="12dp" 设置内容的顶部内边距-->
```



### 七.嵌套滑动的原理

传统的事件分发是从上往下分发，而嵌套滑动事件是从下到上，也就是说，当一个View产生了一个嵌套滑动事件，首先会报告给它的父View，询问它父View是否处理这个事件，如果处理的话，那么子Viw就不处理了（实际上存在父View值处理部分滑动距离的情况）

#### 1.嵌套滑动主要用到的接口和类

1.NestedScrollingChild ：如果一个View想要能够产生嵌套滑动事件，这个View必须实现NestedScrollChild接口，从Android 5.0开始，View实现了这个接口，不需要我们手动实现

2.NestedScrollingParent ：这个接口通常用来被ViewGroup来实现，表示能够接收子View发送过来的嵌套滑动事件

3.NestedScrollingChildHelper ：这个类通常在实现NestedScrollingChild接口的View里面使用，负责将子View产生的嵌套滑动事件报告给父View

4.NestedScrollingParentHelper ：这个类通常在实现NestedScrollingParent 接口的View里面使用，如果父View不想处理一个事件，通过NestedScrollingParentHelper 类帮助传递

#### 2.子View事件的产生和传递

​	整个事件传递过程中，首先能保证传统的事件能够到达该View，当一个事件序列开始时，首先会调用startNestedScroll方法来告诉父View，马上就要开始一个滑动事件了，请问爸爸需要处理，如果处理的话，会返回true，不处理返回fasle。跟传统的事件传递一样，如果不处理的话，那么该事件序列的其他事件都不会传递到父View里面。

​	然后就是调用dispatchNestedPreScroll方法，这个方法调用时，子View还未真正滑动，所以这个方法的作用是子View告诉它的爸爸，此时滑动的距离已经产生，爸爸你看看能消耗多少，然后父View会根据情况消耗自己所需的距离，如果此时距离还未消耗完，剩下的距离子View来消耗，子View滑动完毕之后，会调用dispatchNestedScroll方法来告诉父View，爸爸，我已经滑动完毕，你看看你有什么要求没？这个过程里面可能有子View未消耗完的距离。

​	其次就是fling事件产生，过程跟上面也是一样，也是先调用dispatchNestedPreFling方法来询问父View是否有所行动，然后调用dispatchNestedFling告诉父View，子View已经fling完毕。
  最后就是调用stopNestedScroll表示本次事件序列结束。
  整个过程中，我们会发现子View开始一个动作时，会询问父View是否有所表示，结束一个动作时，也会告诉父View，自己的动作结束了，父View是否有所指示。

```
public interface NestedScrollingChild {
    /**
     * 设置当前View是否能够产生嵌套滑动的事件
     * @param enabled true表示能够产生嵌套滑动的事件，反之则不能
     */
    void setNestedScrollingEnabled(boolean enabled);

    /**
     * 判断当前View是否能够产生嵌套滑动的事件
     * @return
     */
    boolean isNestedScrollingEnabled();

    /**
     * 当嵌套事件开始产生时会调用这个方法，这个方法通常是在ACTION_DOWN里面被调用
     * @param axes axes表示方向，如果(nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0 表示当前滑动方向是垂直方向
     *            ，水平方向也是如此
     * @return 返回true表示有父View能够处理传递传递上去的嵌套滑动事件，实际上这个这个方法里面调用NestedScrollingParent的onStartNestedScroll
     * 方法来判断是否有父View能够处理，这个在后面源码分析时，我们具体讲解
     */
    boolean startNestedScroll(@ViewCompat.ScrollAxis int axes);

    /**
     * 这个方法表示本次嵌套滑动的行为结束了，通常在ACTION_UP或者ACTION_CANCEL里面调用
     */
    void stopNestedScroll();

    /**
     * 判断是否能够处理嵌套滑动的父View
     * @return true表示有，反之则没有
     */
    boolean hasNestedScrollingParent();

    /**
     * 本方法在产生嵌套滑动的View已经滑动完成之后调用，该方法的作用是将剩余没有消耗的距离继续分发到父View里面去
     * @param dxConsumed 表示该View在x轴上消耗的距离
     * @param dyConsumed 表示该View在y轴上消耗的距离
     * @param dxUnconsumed 表示该View在x轴上未消耗的距离
     * @param dyUnconsumed 表示该View在y轴未消耗的距离
     * @param offsetInWindow 表示该该View在屏幕上滑动的距离，包括x轴上的距离和y轴上的距离
     * @return true表示父View消耗这部分的未消耗的距离，反之表示父View不消耗
     */
    boolean dispatchNestedScroll(int dxConsumed, int dyConsumed,
            int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow);

    /**
     * 这个方法在方法调用之前调用，也就是调用这个方法时，滑动距离产生了，但是该View还未滑动。
     * 这个方法的作用是将滑动的距离报给父View，看看父View是否优先消耗这个这部分距离
     * @param dx x轴上产生的距离
     * @param dy y轴上产生的距离
     * @param consumed index为0的值表示父View在x轴消耗的的距离，index为1的值表示父View在y轴上消耗的距离
     * @param offsetInWindow 该View在屏幕滑动的距离
     * @return true表示父View有消耗距离，false表示父View不消耗
     */
    boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed,
            @Nullable int[] offsetInWindow);

    /**
     * 如果父View不对fling事件做任何处理，那么子View会调用这个方法，这个方法的作用是报告父View，子View此时在fling
     * 然而具体是否在fling，还要consumed为true还是false，在这方法里面会调用NestedScrollingParent的onNestedFling
     * @param velocityX x轴上的速度
     * @param velocityY y轴的速度
     * @param consumed true表示子View对这个fling事件有所行动，false表示没有行动
     * @return
     */
    boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed);

    /**
     * 在子View对fling有所行动之前，会调用这个方法。这个方法的作用是，用来询问父View是否对fling事件有所行动
     * @param velocityX
     * @param velocityY
     * @return
     */
    boolean dispatchNestedPreFling(float velocityX, float velocityY);
}
```

#### 3.父View事件的接收和消耗

在系统中，没有特定ViewGroup用来接收和消耗子View传递的事件。因此，只能自己动手了。

```
public class NestedScrollLinearLayout extends LinearLayout implements NestedScrollingParent {
  private static final int OFFSET = 200;
  private NestedScrollingParentHelper mNestedScrollingParentHelper;

  public NestedScrollLinearLayout(Context context) {
    super(context);
  }

  public NestedScrollLinearLayout(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public NestedScrollLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
    return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
  }

  @Override
  public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
    //向下
    if (dy < 0) {
      if (getTranslationY() >= 0) {
        consumed[0] = 0;
        consumed[1] = (int) Math.max(getTranslationY() - OFFSET, dy);
        setTranslationY(getTranslationY() - consumed[1]);
      }
    } else {
      if (getTranslationY() <= OFFSET) {
        consumed[0] = 0;
        consumed[1] = (int) Math.min(dy, getTranslationY());
        setTranslationY(getTranslationY() - consumed[1]);
      }
    }
  }

  @Override
  public void onNestedScrollAccepted(View child, View target, int axes) {
    getNestedScrollingParentHelper().onNestedScrollAccepted(child, target, axes);
  }

  @Override
  public void onStopNestedScroll(View child) {
    getNestedScrollingParentHelper().onStopNestedScroll(child);
  }


  @Override
  public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
  }

  @Override
  public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
    return false;
  }

  @Override
  public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
    return false;
  }

  private NestedScrollingParentHelper getNestedScrollingParentHelper() {
    if (mNestedScrollingParentHelper == null) {
      mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
    }
    return mNestedScrollingParentHelper;
  }
}
```

在整个实现过程中，我们发现，我们只对onStartNestedScroll方法和onNestedPreScroll方法做了我们自己的实现，其他的要么空着，要么就是通过NestedScrollingParentHelper来帮助我们来实现。整个过程比较清晰和明了。
  不过，这其中，我们需要注意的是，每个方法的含义和调用的时机。`onStartNestedScroll`方法对应子View的`startNestedScroll`方法,当子View调用`startNestedScroll`方法会回调父View的`onStartNestedScroll`方法。其他方法也是类似的，不过需要注意的是，通常子View的方法都是以dispatch开头的，父View的方法都是以on开头的。
  对于NestedScrollingParnet这一块，感觉没有需要注意的，因为这部分需要咱们自己实现，而实现这部分的功能，需要了解子View的是怎么将事件传递到父View。

[Android 源码分析 - 嵌套滑动机制的实现原理]: https://www.jianshu.com/p/cb3779d36118

### 八.自定义控件

#### 1.五种自定义View的类型

**1.自绘控件**

View所展现的内容全部是自己绘制出来，主要代码写在onDraw()方法里面，通常直接继承View（比如：卡片，动画展示等）

**2.组合控件**

不需要自己去绘制视图上的内容，将系统原生的控件组合到一起

**3.继承控件**

继承现有的控件，增加一些新的功能（比如继承ImageView）

**4.事件类控件**

通常需要处理触摸事件，并且会消费事件。大多数事件类控件需要结合重绘方法来进行，比如：刮刮乐

**5.容器类控件**

为实现具体开发而开发的自定义容器，比如百分比布局

#### 2.自定义View须知

**1.让View支持wrap_content**

直接继承View或者ViewGroup的控件，如果不在onMeasure对wrap_content做特殊处理，那么当外界在布局时，使用wrap_content就无法达到预期效果。如果不处理，wrap_content实际效果和match_parent一样，可以通过获取SpecMode，如果是AT_MOST,那么设置固定值

**2.如果有必要，让View支持padding**

因为直接继承View，如果不在draw方法中处理padding，那么padding属性是失效的，如果是继承ViewGroup，则需要考虑在onMeasure和onLayout中考虑padding和子元素的margin对其造成的影响，不然padding和margin将失效

**3.尽量不要在View中使用Handler，因为没必要**

因为View中已经提供了post系列的方法，除非很明确需要用handler来发送消息

**4.如果View中有线程或者动画，需要及时停止**

不然很容易导致内存泄漏，在onDetachedFromWindow方法在停止线程和动画

**5.如果View带有滑动嵌套，需要处理好滑动冲突**



### 九.RecyclerView

RecyclerView一般做为Android显示列表的控件，有诸多优异的性能

1.回收池策略能加载上亿级数据并不发生卡顿

2.适配器模式能展示任意显示需求

架构：充分利用传送带原理，只有用户看到的数据才会加载到内存，看不到的在等待被加载，**传送带的工作机制可以比作生产者与消费者模式**

#### 1.核心组件

1.**回收池** : 能回收任意Item控件，并返回符合类型的Item控件；比如onBinderViewHolder方法中的第一个参数是从回收池返回的 。涉及到集合查找，用栈，会比较快。因为一个item从屏幕移出，会马上放进回收池，但是马上会有另一个item需要加载，这个时候，如果这个item的type跟刚刚入栈的那个一样，就能方便直接拿出。

2.**适配器**：Adapter接口，辅助RecyclerView实现列表展示，将用户界面和交互进行分离

3.**RecyclerView**：做触摸事件的交互，主要实现边界判断，根据用户的触摸反馈，协调回收池对象与适配器对象直接的工作


