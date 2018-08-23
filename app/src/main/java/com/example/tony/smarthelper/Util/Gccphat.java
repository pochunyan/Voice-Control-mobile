package com.example.tony.smarthelper.Util;

public class Gccphat {


    private Complex[] s1 = null;
    private Complex[] s2 = null;
    private Complex[] s_general = null; // abs term
    private double lag_return = 0;
    private int t_Len;
    private int Real_Len = 0;

    public Gccphat(double sig1[] , double sig2[])
    {
        //int maxLen = sig1.length > sig2.length ? sig1.length : sig2.length;
        int maxLen = sig1.length;
        int testLen = maxLen;
        //System.out.printf("maxLen:%d \n", maxLen);
        maxLen = (int) (Math.log(maxLen)/ Math.log(2.0)) + 1;//求出FFT的幂次
        //System.out.printf("maxLen:%d \n", maxLen);
        maxLen = (int) Math.pow(2, maxLen);
        System.out.printf("max Len : %d \n", maxLen);
        t_Len = maxLen;
        //System.out.printf("maxLen:%d \n", maxLen);

        s1 = new Complex[maxLen];
        s2 = new Complex[maxLen];
        s_general = new Complex[maxLen];
        for(int i = 0;i < maxLen;i++)
        {
            //这一步已经完成了补零的工作了
            s1[i] = new Complex();
            s2[i] = new Complex();
            s_general[i] = new Complex();
        }

        for(int i=0;i<sig1.length;i++)
        {
            s1[i].real = sig1[i];
            //System.out.println(s1[i].real);
        }
        for(int i=0;i<sig2.length;i++)
        {
            s2[i].real = sig2[i];
            //System.out.println(s2[i].real);
        }

        //求出信号的FFT
        FFT fft = new FFT(maxLen);
        //System.out.printf("fft_N: %d\n", fft);
        fft.FFT(s1);
        fft.FFT(s2);
        conj(s2);
        mul(s1,s2); // S1*S2'

        // General the value  :          S1*S2'
        //                    :   G =  ----------
        //                    :        |S1 * S2'|

        for (int i = 0; i < s_general.length; i++){
            s_general[i].real = Math.abs(s1[i].real);
            s1[i].real = s1[i].real / s_general[i].real;
            s1[i].imag = s1[i].imag / s_general[i].real;
        }
        fft.IFFT(s1);

        //System.out.printf("ifft Len : %d \n", s1.length);

        double max = 0;
/*
        for(int i = 0; i < maxLen; i++)
        {
            if(Math.abs(s1[i].real) > max)
            {
                lag_return = i;
                Real_Len = i;
                max = s1[i].real;
            }
            //System.out.printf("s[%d]:%f \n", i, s1[i].real);
        }
        //System.out.printf("max : %f \n", max);
*/

        for(int i = 0; i < 13; i++)
        {
            if(s1[i].real > max)
            {
                lag_return = i;
                Real_Len = i;
                max = s1[i].real;
            }
            //System.out.printf("s[%d]:%f \n", i, s1[i].real);
        }
        for(int i = maxLen - 12; i < maxLen; i++)
        {
            if(s1[i].real > max)
            {
                lag_return = i;
                Real_Len = i;
                max = s1[i].real;
            }
            //System.out.printf("s[%d]:%f \n", i, s1[i].real);
        }

    }

    public int RealPosition(){
        return Real_Len;
    }
    public int getLen(){
        return t_Len;
    }
    public double getLag()
    {
        return lag_return;
    }
    //求两个复数的乘法，结果返回到第一个输入
    public void mul(Complex[] s1,Complex[] s2)
    {
        double temp11 = 0, temp12 = 0;
        double temp21 = 0, temp22 = 0;
        for(int i = 0; i < s1.length; i++)
        {
            temp11 = s1[i].real ; temp12 = s1[i].imag;
            temp21 = s2[i].real ; temp22 = s2[i].imag;
            s1[i].real = temp11 * temp21 - temp12 * temp22;
            s1[i].imag = temp11 * temp22 + temp21 * temp12;
            //s1[i].real = s1[i].real * s2[i].real - s1[i].imag * s2[i].imag;
            //s1[i].imag = s1[i].real * s2[i].imag + s1[i].imag * s2[i].real;
        }
    }
    //求信号的共軛
    public void conj(Complex s[])
    {
        for(int i = 0; i < s.length; i++)
        {
            s[i].imag = 0.0f - s[i].imag;
        }
    }
}
