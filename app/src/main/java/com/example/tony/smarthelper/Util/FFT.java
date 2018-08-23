package com.example.tony.smarthelper.Util;

public class FFT {
    private int N_FFT = 0;				    //傅里叶变换的点数
    private int M_of_N_FFT = 0;			    //蝶形运算的级数，N = 2^M
    private int Npart2_of_N_FFT = 0;		//创建正弦函数表时取PI的1/2
    private int Npart4_of_N_FFT = 0;		//创建正弦函数表时取PI的1/4
    private double SIN_TABLE_of_N_FFT [] = null;

    private static final double PI = Math.PI;

    public FFT(int FFT_N)
    {
        Init_FFT(FFT_N);
    }
    public Complex[] complexLization(double data[])
    {
        Complex[] w = new Complex[data.length];
        for(int i = 0; i < data.length; i++)
        {
            w[i].real = data[i];
            w[i].imag = 0.0f;
        }
        return w;
    }
    public double[] magnitude(Complex[] data)
    {
        double[] r = new double[data.length];
        for(int i=0;i<data.length;i++)
        {
            r[i] = Math.sqrt(data[i].real * data[i].real + data[i].imag * data[i].imag);
        }
        return r;
    }
    //初始化FFT程序
    //N_FFT是 FFT的点数，必须是2的次方
    private void Init_FFT(int N_of_FFT)
    {
        int temp_N_FFT=1;
        N_FFT = N_of_FFT;					//傅里叶变换的点数 ，必须是2的次方
        M_of_N_FFT = 0;					//蝶形运算的级数，N = 2^M
        for (int i = 0; temp_N_FFT < N_FFT; i++)
        {
            temp_N_FFT = 2 * temp_N_FFT;
            M_of_N_FFT++;
        }
        //printf("\n%d\n",M_of_N_FFT);
        Npart2_of_N_FFT = N_FFT/2;		//创建正弦函数表时取PI的1/2
        Npart4_of_N_FFT = N_FFT/4;		//创建正弦函数表时取PI的1/4

        //data_of_N_FFT = (ptr_complex_of_N_FFT)malloc(N_FFT * sizeof(complex_of_N_FFT));
        //data_of_N_FFT =
        //ptr_complex_of_N_FFT SIN_TABLE_of_N_FFT=NULL;
        //SIN_TABLE_of_N_FFT = (ElemType *)malloc((Npart4_of_N_FFT+1) * sizeof(ElemType));
        SIN_TABLE_of_N_FFT = new double[Npart4_of_N_FFT + 1];
        CREATE_SIN_TABLE();				//创建正弦函数表
    }
    //创建正弦函数表
    private void CREATE_SIN_TABLE()
    {
        int i = 0;
        for (i = 0; i <= Npart4_of_N_FFT; i++)
        {
            SIN_TABLE_of_N_FFT[i] = Math.sin(PI*i/Npart2_of_N_FFT);//SIN_TABLE[i] = sin(PI2*i/N);
        }
    }

    private double Sin_find(double x)
    {
        int i = (int)(N_FFT * x);
        i = i >> 1;
        if (i > Npart4_of_N_FFT)//注意：i已经转化为0~N之间的整数了！
        {
            //不会超过N/2
            i = Npart2_of_N_FFT - i;//i = i - 2*(i-Npart4);
        }
        return SIN_TABLE_of_N_FFT[i];
    }

    private double Cos_find(double x)
    {
        int i = (int)(N_FFT*x);
        i = i>>1;
        if (i < Npart4_of_N_FFT)//注意：i已经转化为0~N之间的整数了！
        {
            //不会超过N/2
            //i = Npart4 - i;
            return SIN_TABLE_of_N_FFT[Npart4_of_N_FFT - i];
        }

        else//i>Npart4 && i<N/2
        {
            //i = i - Npart4;
            return -SIN_TABLE_of_N_FFT[i - Npart4_of_N_FFT];
        }
    }
    private void ChangeSeat(Complex DataInput[])
    {
        int nextValue,nextM,i,k,j=0;
        Complex temp;

        nextValue=N_FFT/2;                  //变址运算，即把自然顺序变成倒位序，采用雷德算法
        nextM=N_FFT-1;
        for (i=0;i<nextM;i++)
        {
            if (i<j)					//如果i<j,即进行变址
            {
                temp=DataInput[j];
                DataInput[j]=DataInput[i];
                DataInput[i]=temp;
            }
            k=nextValue;                //求j的下一个倒位序
            while (k<=j)				//如果k<=j,表示j的最高位为1
            {
                j=j-k;					//把最高位变成0
                k=k/2;					//k/2，比较次高位，依次类推，逐个比较，直到某个位为0
            }
            j=j+k;						//把0改为1
        }
    }

    //FFT运算函数
    public void FFT(Complex []data)
    {
        int L=0,B=0,J=0,K=0;
        int step=0, KB=0;
        //ElemType P=0;
        double angle;
        Complex W = new Complex();
        Complex Temp_XX = new Complex();

        ChangeSeat(data);//变址
        //CREATE_SIN_TABLE();
        for (L=1; L<=M_of_N_FFT; L++)
        {
            step = 1 << L; //2^L
            B = step >> 1; //B = 2^(L-1)
            for (J=0; J<B; J++)
            {
                //P = (1<<(M-L))*J; //P = 2^(M-L) *J
                angle = (double)J/B;			//这里还可以优化
                W.imag =  -Sin_find(angle);		//用C++该函数课声明为inline
                W.real =   Cos_find(angle);		//用C++该函数课声明为inline
                //W.real =  cos(angle*PI);
                //W.imag = -sin(angle*PI);
                for (K=J; K<N_FFT; K=K+step)
                {
                    KB = K + B;
                    //Temp_XX = XX_complex(data[KB],W);
                    //用下面两行直接计算复数乘法，省去函数调用开销
                    Temp_XX.real = data[KB].real * W.real-data[KB].imag*W.imag;
                    Temp_XX.imag = W.imag*data[KB].real + data[KB].imag*W.real;

                    data[KB].real = data[K].real - Temp_XX.real;
                    data[KB].imag = data[K].imag - Temp_XX.imag;

                    data[K].real = data[K].real + Temp_XX.real;
                    data[K].imag = data[K].imag + Temp_XX.imag;
                }
            }
        }
    }

    //IFFT运算函数
    public void IFFT(Complex []data)
    {
        int L=0,B=0,J=0,K=0;
        int step=0, KB=0;
        //ElemType P=0;
        double angle=0.0f;
        Complex W = new Complex();
        Complex Temp_XX = new Complex();

        ChangeSeat(data);//变址
        //CREATE_SIN_TABLE();
        for (L = 1; L <= M_of_N_FFT; L++)
        {
            step = 1 << L;//2^L
            B = step >> 1;//B=2^(L-1)
            for (J = 0; J < B; J++)
            {
                //P = (1<<(M-L))*J;//P=2^(M-L) *J
                angle = (double) J/B;			//这里还可以优化
                //System.out.println("angle:" + angle);

                //W.imag =   Sin_find(angle);		//用C++该函数课声明为inline
                //W.real =   Cos_find(angle);		//用C++该函数课声明为inline
                W.imag = Sin_find(angle);
                W.real = Cos_find(angle);

                for (K = J; K < N_FFT; K = K + step)
                {
                    KB = K + B;
                    //Temp_XX = XX_complex(data[KB],W);
                    //用下面两行直接计算复数乘法，省去函数调用开销
                    Temp_XX.real = data[KB].real*W.real - data[KB].imag*W.imag;
                    Temp_XX.imag = W.imag*data[KB].real + data[KB].imag*W.real;

                    data[KB].real = data[K].real - Temp_XX.real;
                    data[KB].imag = data[K].imag - Temp_XX.imag;

                    data[K].real = data[K].real + Temp_XX.real;
                    data[K].imag = data[K].imag + Temp_XX.imag;
                }
            }
        }
    }
}
