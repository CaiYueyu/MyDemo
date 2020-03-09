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

##### 2.RadialGradient环形渲染

```
RadialGradient(float centerX, float centerY, float radius, @ColorInt int colors[], @Nullable float stops[], TileMode tileMode)
```

（centerX，centerY）：shader的中心坐标，开始渐变的坐标

（radius）：渐变的半径

（centerColor，edgeColor）：中心点渐变的颜色值，边界渐变的颜色值

（colors）：渐变颜色数组

（stops）：渐变位置数组，类似扫描渐变的positions数组，取值[0,1]，中心点为0，半径到达位置为1.0f

（tileMode）：同线性渲染的tile

##### 3.SweepGradient扫描渲染

```
SweepGradient(float cx, float cy, @ColorInt int color0,int color1)
```

（cx，cy）：渐变中心坐标

（color0，color1）：渐变起始和结束的颜色值

（colors，positions）：类似LinearGradient，用于多颜色渐变，positions为null是，根据颜色线性渐变

##### 4.BitmapShader位图渲染

```
BitmapShader(@NonNull Bitmap bitmap, @NonNull TileMode tileX, @NonNull TileMode tileY)
```

（bitmap）：构造shader使用的bitmap

（tileX）：X轴方向的TileMode

（tileY）：Y轴方向的TileMode

##### 5.ComposeShader组合渲染

```
ComposeShader(@NonNull Shader shaderA, @NonNull Shader shaderB, Xfermode mode)
ComposeShader(@NonNull Shader shaderA, @NonNull Shader shaderB, PorterDuff.Mode mode)
```

（shaderA，shaderB）：要混合的两种shader

（Xfermode mode）：组合两种shader颜色的模式

（PorterDuff.Mode mode）：组合两种shader颜色的模式

#### 4.Paint 颜色相关

##### 1.图层混合模式 PorterDuff.Mode

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

##### 2.图层混合的18种模式

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

##### 3.滤镜效果 LightColorFilter

1.作用：可以模仿光照效果

2.构造方法：public LightingColorFilter(@ColorInt int mul, @ColorInt int add)

   参数mul和add都是和颜色值格式相同的int值，其中mul用来和目标像素相乘，add用来和目标像素相加：

如：R' = R *mul.R/0xff + add.R;  G' = G * mul.G + add.G;  B' = B * mul.B/0xff + add.B

3.使用方法

```
LightingColorFilter lighting = new LightingColorFilter(0x00ffff,0x000000)
paint.setColorFilter(lighting)
```

##### 4.PorterDuffColorFilter滤镜（图层混合）

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

##### 5.ColorMatrixColorFilter滤镜（颜色数组）

1.作用：处理图像色彩效果

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