import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import javax.swing.plaf.SliderUI;


public class imageReader {

   static int divide_size;
   static JFrame frame;
   static JLabel label,label2,label3;
   static int width,height;
   static BufferedImage img;
   static int flag=0;
   
   public static void main(String[] args) {
   	

/*	String fileName = args[0];
   	int width = Integer.parseInt(args[1]);
	int height = Integer.parseInt(args[2]);
	long sleep_time=
*/
	int quantization_number=3;
	int case_no=1;
	long sleep_time=1;
	String fileName="nimage1.rgb";
	width=352;
	height=288;
	
	/*String fileName="image1.rgb";
	width=640;
	height=480;
	*/
	
	
	divide_size=(width*height)/64;
	double[][][]sub_blocks_ro= new double[divide_size][8][8];
	double[][][]sub_blocks_go= new double[divide_size][8][8];
	double[][][]sub_blocks_bo= new double[divide_size][8][8];
		
	double[][][]sub_blocks_r_DCT= new double[divide_size][8][8];
	double[][][]sub_blocks_g_DCT= new double[divide_size][8][8];
	double[][][]sub_blocks_b_DCT= new double[divide_size][8][8];
	
	
	double [][][]sub_blocks_r_iDCT= new double[divide_size][8][8];
	double [][][]sub_blocks_g_iDCT= new double[divide_size][8][8];
	double [][][]sub_blocks_b_iDCT= new double[divide_size][8][8];
	
	img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	BufferedImage img2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	BufferedImage img3 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	
	BufferedImage img_ro = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	BufferedImage img_go = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	BufferedImage img_bo = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

	double[][] ro=new double[height][width];
	double[][] go=new double[height][width];
	double[][] bo=new double[height][width];
	
	double[][][] sub_blocks_DCT=new double[divide_size][8][8];
	double[][][] sub_blocks_DCT2=new double[divide_size][8][8];
	double[][][] sub_blocks_iDCT=new double[divide_size][8][8];
	
	
   

    try {
	    File file = new File(fileName);
	    InputStream is = new FileInputStream(file);

	    long len = file.length();
	    byte[] bytes = new byte[(int)len];
	    
	     
	    int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
    		
        
    	int ind = 0;
		for(int y = 0; y < height; y++){
	
			for(int x = 0; x < width; x++){
		 
				byte a = 0;
				byte r = bytes[ind];
				byte g = bytes[ind+height*width];
				byte b = bytes[ind+height*width*2]; 
				
				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				
				img.setRGB(x,y,pix);
				ro[y][x]=r;
				go[y][x]=g;
				bo[y][x]=b;
				
				ind++;
			}
		}
		
		
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    frame = new JFrame();
    
    divide_image(ro,sub_blocks_ro);
    divide_image(go,sub_blocks_go);
    divide_image(bo,sub_blocks_bo);
    
    calculate_dct(sub_blocks_ro,sub_blocks_r_DCT);
    calculate_dct(sub_blocks_go,sub_blocks_g_DCT);
    calculate_dct(sub_blocks_bo,sub_blocks_b_DCT);
    quantize_image(sub_blocks_r_DCT,sub_blocks_g_DCT,sub_blocks_b_DCT,quantization_number);
    
    
    dequantize_image(sub_blocks_r_DCT,sub_blocks_g_DCT,sub_blocks_b_DCT,quantization_number);
    calculate_idct(sub_blocks_r_DCT,sub_blocks_r_iDCT);
    calculate_idct(sub_blocks_g_DCT,sub_blocks_g_iDCT);
    calculate_idct(sub_blocks_b_DCT,sub_blocks_b_iDCT);
 
    merge_3darrays(sub_blocks_r_iDCT,sub_blocks_g_iDCT,sub_blocks_b_iDCT,sub_blocks_iDCT);
       
	label = new JLabel(new ImageIcon(img));
	frame.getContentPane().add(label, BorderLayout.WEST);
    frame.pack();
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  
    sequential_decoding(sub_blocks_iDCT,img2,10);
    

}
  

static void quantize_image(double[][][] sub_blocks_r_DCT,
		double[][][] sub_blocks_g_DCT, double[][][] sub_blocks_b_DCT, int i) {
	quantize(sub_blocks_r_DCT, i);
    quantize(sub_blocks_g_DCT, i);
    quantize(sub_blocks_b_DCT, i);
}


static void dequantize_image(double[][][] sub_blocks_r_iDCT,
		double[][][] sub_blocks_g_iDCT, double[][][] sub_blocks_b_iDCT, int i) {
	// TODO Auto-generated method stub
	dequantize(sub_blocks_r_iDCT, i);
    dequantize(sub_blocks_g_iDCT, i);
    dequantize(sub_blocks_b_iDCT, i);
}


static void sequential_decoding(double[][][] sub_blocks_DCT,
		BufferedImage img2, long sleep_time) {
	
	array3d_to_img(sub_blocks_DCT,img2,sleep_time);    							
}






static void merge_3darrays(double[][][] sub_blocks_r_DCT,double[][][] sub_blocks_g_DCT, double[][][] sub_blocks_b_DCT,double[][][] sub_blocks_DCT) {
	
	int q,x,y;
	
	for(q=0;q<divide_size;q++)
	{
		for(y=0;y<8;y++)
		{
			for(x=0;x<8;x++)
			{
				byte a = 0;
				byte r =  (byte) sub_blocks_r_DCT[q][y][x];
				byte g =  (byte) sub_blocks_g_DCT[q][y][x];
				byte b =  (byte) sub_blocks_b_DCT[q][y][x]; 
				
				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				
				sub_blocks_DCT[q][y][x]=pix;
			}
		}
	}
	
}


static void array3d_to_img(double[][][] sub_blocks_DCT,BufferedImage img_temp,long sleep_time)
{
	  int xo=0,yo=0,i=0,j=0,x=0,y=0,q=0;
	  int hn=height/8;
	  int wn=width/8;
	  
	  label2 = new JLabel(new ImageIcon(img_temp));
	  frame.getContentPane().add(label2, BorderLayout.EAST);
	  frame.pack();
		
	  for(j=0;j<hn;j++)
	  {
		  for(i=0;i<wn;i++)
		  {
			  for(y=0,yo=(j*8);(y<8)&&(yo<height);yo++,y++)
			  {
				  for(x=0,xo=(i*8);(x<8)&&(xo<width);xo++,x++)
				  {
					  img_temp.setRGB(xo, yo,(int) sub_blocks_DCT[q][y][x]);
				  }
			  }
			  q++;
					  try {
						Thread.sleep(sleep_time);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 label2.setIcon(new ImageIcon(img_temp));
					    
		  }
	  }
}


static void array3d_to_img(double[][][] sub_blocks_DCT,BufferedImage img_temp)
{
	int xo=0,yo=0,i=0,j=0,x=0,y=0,q=0;
	  int hn=height/8;
	  int wn=width/8;
	  for(j=0;j<hn;j++)
	  {
		  for(i=0;i<wn;i++)
		  {
			  for(y=0,yo=(j*8);(y<8)&&(yo<height);yo++,y++)
			  {
				  for(x=0,xo=(i*8);(x<8)&&(xo<width);xo++,x++)
				  {
					  img_temp.setRGB(xo, yo,(int) sub_blocks_DCT[q][y][x]);
					  //HERE
				  }
			  }
			  q++;
		  }
	  }
	  
	  if(flag==0) 
	  {
		//  label2.setIcon(new ImageIcon(img_temp));
		  label2 = new JLabel(new ImageIcon(img_temp));
		  frame.getContentPane().add(label2, BorderLayout.CENTER);
		  
	  }
	  else
	  {
		  label3 = new JLabel(new ImageIcon(img_temp));
		  frame.getContentPane().add(label3, BorderLayout.EAST);  
	  }
		 flag++;
	  
	
	   frame.pack();  

}


static void calculate_dct(double[][][] sub_blocks_each_channel, double[][][] sub_blocks_DCT) {
	
	double[] c = new double[8];
	initializeCoefficients(c);     
	for(int q=0;q<divide_size;q++)
	{
		 for (int u = 0; u <=7; u++) 
         {
             for (int v = 0; v <=7; v++)
             {
                 double sum=0;
                 for (int x = 0; x <=7; x++) 
                 {
                     for (int y = 0; y <=7; y++) 
                     {
                         sum += ((sub_blocks_each_channel[q][x][y])*(Math.cos((((2*x)+1)*(u)*Math.PI)/16))*(Math.cos((((2*y)+1)*(v)*Math.PI)/16)));
                      }
                  }                 
                  sum = sum * (c[u]*c[v]*.25);
                  sub_blocks_DCT[q][u][v] =sum;
                        
                }
            }
		
		
		
	}	
	
}




static void calculate_idct(double[][][] sub_blocks_DCT, double[][][] sub_blocks_iDCT) {
	
 double[] c = new double[8];
	initializeCoefficients(c);     
	for(int q=0;q<divide_size;q++)
	{	 
		for (int x = 0; x <= 7; x++) 
        {
            for (int y = 0; y <= 7; y++) 
            {
                double sum=0;
                for (int u = 0; u <= 7; u++) 
                {
                    for (int v = 0; v <= 7; v++) 
                    {
                      sum += ( (c[u] * c[v]) * (sub_blocks_DCT[q][u][v] * (Math.cos((((2*x)+1)*u*Math.PI)/16)) * (Math.cos((((2*y)+1)*v*Math.PI)/16))));
                    }
                }
                sum = sum * 0.25;                
                
                if(sum>127)
                	sum=(byte)127;//Math.round(127);
                else if(sum<-128)
                	sum=(byte)-128;//Math.round(-128);
                else
                	sum=(byte)Math.round(sum);
                
                sub_blocks_iDCT[q][x][y] = sum;
             }

         }	
	}
}






   static void divide_image(BufferedImage img, double[][][] sub_blocks_ro)
   {
	  int xo=0,yo=0,i=0,j=0,x=0,y=0,q=0;
	  int hn=height/8;
	  int wn=width/8;
	  for(j=0;j<hn;j++)
	  {
		  for(i=0;i<wn;i++)
		  {
			  for(y=0,yo=(j*8);(y<8)&&(yo<height);yo++,y++)
			  {
				  for(x=0,xo=(i*8);(x<8)&&(xo<width);xo++,x++)
				  {
					  sub_blocks_ro[q][y][x]=img.getRGB(xo, yo);
				  }
			  }
			  q++;
		  }
	  }
	  
   }
   
   static void dequantize(double[][][] sub_blocks_r_iDCT,int n) {
		// TODO Auto-generated method stub
		int q,x,y;
		for(q=0;q<divide_size;q++)
		{
			for(y=0;y<8;y++)
			{
				for(x=0;x<8;x++)
				{
					sub_blocks_r_iDCT[q][y][x]=( (sub_blocks_r_iDCT[q][y][x]*(Math.pow(2, n))));
				}
			}
		}
		
	}



	static void quantize(double[][][] sub_blocks_r_DCT,int n) {
			//F'[u, v] = round ( F[u, v] / 2N).
		int q,x,y;
		for(q=0;q<divide_size;q++)
		{
			for(y=0;y<8;y++)
			{
				for(x=0;x<8;x++)
				{
					sub_blocks_r_DCT[q][y][x]=((sub_blocks_r_DCT[q][y][x]/(Math.pow(2, n))));
				}
			}
		}
		
	}
/*Repeated functions..****************************************************************************************/	
/*************************************************************************************************************/
	
	static void divide_image(double[][] img_temp, double[][][] sub_blocks)
	   {
		  int xo=0,yo=0,i=0,j=0,x=0,y=0,q=0;
		  int hn=height/8;
		  int wn=width/8;
		  for(j=0;j<hn;j++)
		  {
			  for(i=0;i<wn;i++)
			  {
				  for(y=0,yo=(j*8);(y<8)&&(yo<height);yo++,y++)
				  {
					  for(x=0,xo=(i*8);(x<8)&&(xo<width);xo++,x++)
					  {
						  sub_blocks[q][y][x]= img_temp[yo][xo];
					  }
				  }
				  q++;
			  }
		  }
		  
	   }
	  

	static void initializeCoefficients(double[] c) {
	    for (int i=1;i<8;i++) {
	        c[i]=1;
	    }
	    c[0]=1/Math.sqrt(2.0);
	}

}


