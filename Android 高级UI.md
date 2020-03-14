

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

###### 1.LinearGradient线性渲染

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

###### 2.RadialGradient环形渲染

```
RadialGradient(float centerX, float centerY, float radius, @ColorInt int colors[], @Nullable float stops[], TileMode tileMode)
```

（centerX，centerY）：shader的中心坐标，开始渐变的坐标

（radius）：渐变的半径

（centerColor，edgeColor）：中心点渐变的颜色值，边界渐变的颜色值

（colors）：渐变颜色数组

（stops）：渐变位置数组，类似扫描渐变的positions数组，取值[0,1]，中心点为0，半径到达位置为1.0f

（tileMode）：同线性渲染的tile

###### 3.SweepGradient扫描渲染

```
SweepGradient(float cx, float cy, @ColorInt int color0,int color1)
```

（cx，cy）：渐变中心坐标

（color0，color1）：渐变起始和结束的颜色值

（colors，positions）：类似LinearGradient，用于多颜色渐变，positions为null是，根据颜色线性渐变

###### 4.BitmapShader位图渲染

```
BitmapShader(@NonNull Bitmap bitmap, @NonNull TileMode tileX, @NonNull TileMode tileY)
```

（bitmap）：构造shader使用的bitmap

（tileX）：X轴方向的TileMode

（tileY）：Y轴方向的TileMode

###### 5.ComposeShader组合渲染

```
ComposeShader(@NonNull Shader shaderA, @NonNull Shader shaderB, Xfermode mode)
ComposeShader(@NonNull Shader shaderA, @NonNull Shader shaderB, PorterDuff.Mode mode)
```

（shaderA，shaderB）：要混合的两种shader

（Xfermode mode）：组合两种shader颜色的模式

（PorterDuff.Mode mode）：组合两种shader颜色的模式

##### 4.Paint 颜色相关

###### 1.图层混合模式 PorterDuff.Mode

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

###### 2.图层混合的18种模式

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

###### 3.滤镜效果 LightColorFilter

1.作用：可以模仿光照效果

2.构造方法：public LightingColorFilter(@ColorInt int mul, @ColorInt int add)

   参数mul和add都是和颜色值格式相同的int值，其中mul用来和目标像素相乘，add用来和目标像素相加：

如：R' = R *mul.R/0xff + add.R;  G' = G * mul.G + add.G;  B' = B * mul.B/0xff + add.B

3.使用方法

```
LightingColorFilter lighting = new LightingColorFilter(0x00ffff,0x000000)
paint.setColorFilter(lighting)
```

###### 4.PorterDuffColorFilter滤镜（图层混合）

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

###### 5.ColorMatrixColorFilter滤镜（颜色数组）

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