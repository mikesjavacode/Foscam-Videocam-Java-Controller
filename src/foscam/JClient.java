
package foscam;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 *
 * @author Mike
 */
public class JClient extends Thread
{
    public Scanner urlSend;
    
    public String fosCom;
    public String fos1Com;
    public String fos2Com;
    
    public String[] fosCmd;
    public String[] fosParam;
    
    public int cmdNum;
    public int prmNum;
    
    public int[] timeDelay;
    public int timerNum;
    
    // Client constructor
    public JClient()
    {
        // The following three strings are used to put together the complete
        // http request that is sent to the Foscam video camera.
        
        // 1st part of Foscam "non-varying" command string
        fos1Com = "http://75.76.39.251:88/cgi-bin/CGIProxy.fcgi?cmd=";
        // Foscam pan/tilt command string
        fosCmd = new String[30];
        // Foscam parameter command string
        fosParam = new String[30];
        // 2nd part of Foscam "non-varying" command string
        fos2Com = "&usr=mbotts&pwd=2015bottsm*";
        
        // Time delay values for rectangular pattern scan
        timeDelay = new int[2];
        
        // FOSCAM COMMANDS
        
        // PTZ Commands
        fosCmd[0] = "ptzStopRun";
        
        fosCmd[1] = "ptzMoveUp";
        fosCmd[2] = "ptzMoveLeft";

        fosCmd[3] = "ptzMoveDown";
        fosCmd[4] = "ptzMoveRight";
        
        // Speed command
        fosCmd[5] = "setPTZSpeed";
        
        // Cruise commands
        fosCmd[6] = "ptzAddPresetPoint";
        fosCmd[7] = "ptzSetCruiseMap";
        fosCmd[8] = "ptzStartCruise";
        
        // Reset position
        fosCmd[19] = "setPTZSpeed";
        fosCmd[20] = "ptzReset";
        
        // FOSCAM PARAMETERS
        
        // ** Waypoint Settings
        fosParam[0] = "&name=wp1";
        fosParam[1] = "&name=wp2";
        fosParam[2] = "&name=wp3";
        fosParam[3] = "&name=wp4";
        
        // ** Speed Settings 
        // Very slow
        fosParam[4] = "&speed=4";
        // Slow
        fosParam[5] = "&speed=3";
        // Normal
        fosParam[6] = "&speed=2";
        // Fast
        fosParam[7] = "&speed=1";
        // Very fast
        fosParam[8] = "&speed=0";
        
        // ** Cruise Map Settings
        fosParam[9] = "&name=testmap&point0=wp1&point1=wp2&point2=wp3&point3=wp4";
        fosParam[10] = "&mapName=testmap";
        
        // For horizontal scan and stop
        timeDelay[0] = 3000;
        // For vertical scan
        timeDelay[1] = 1500;
    }

    // Method run
    @Override
    public void run()
    {
        timeDelay(1000);
        // Set normal slew speed
        prmNum = 6;
        setSpeed();

        // Move down from default position
        cmdNum = 3;
        moveCam();
        // Delay for 3.5 seconds to allow camera to slew
        timeDelay(3500);

        // Move right to starting position
        cmdNum = 4;
        moveCam();
        // Delay for 1.5 seconds to allow camera to slew
        timeDelay(1500);

        // Stop
        stopMove();
        // Delay for 1.5 seconds
        timeDelay(1500);

        //******************************************************************
        // 1st Speed-based rectangular scan loop
        //******************************************************************
        for( int i = 0; i < 4; i++)
        {
            // Set waypoint
            prmNum = i;
            markWaypoint();

            // Move to next point
            // cmdNum = 1, Up
            // cmdNum = 2, Left
            // cmdNum = 3, Down
            cmdNum = i+1;
            moveCam();
            
            if( (cmdNum%2) == 0)
                timerNum = timeDelay[0];
            else
                timerNum = timeDelay[1];
            timeDelay(timerNum);
        }  
        
        stopMove();
        // Delay for 2.0 seconds 
        timeDelay(2000);
        
        //******************************************************************

        //******************************************************************
        // Reset Foscam position, load Cruise Map, and run Cruise mode
        //******************************************************************

        // Reset position
        resetPosition();
        // Delay for 4.0 seconds to allow camera to slew to position
        timeDelay(4000);
        // Set "very slow" slew speed
        prmNum = 4;
        setSpeed();
        // Set Cruise Map (based on previously set waypoints)
        setCruiseMap();
        // Start Cruise mode
        startCruise();
        
        //******************************************************************
            
    } // end of method run  
    
    //**************************************************************************

    // Method to set the slew speed
    public void setSpeed()
    {
        // Set speed
        cmdNum = 19;
        fosCom = fos1Com+fosCmd[cmdNum]+fos2Com+fosParam[prmNum];
        httpRequest();
    }   

    // Method to mark a waypoint 
    public void markWaypoint()
    {
        // Set waypoint
        cmdNum = 6;
        fosCom = fos1Com+fosCmd[cmdNum]+fos2Com+fosParam[prmNum];
        httpRequest();
    }   
   
    // Method to load the Cruise Map
    public void setCruiseMap()
    {
        cmdNum = 7;
        prmNum = 9;
        fosCom = fos1Com+fosCmd[cmdNum]+fos2Com+fosParam[prmNum];
        httpRequest();
    }

    // Method to start Cruise mode
    public void startCruise()
    {
        cmdNum = 8;
        prmNum = 10;
        fosCom = fos1Com+fosCmd[cmdNum]+fos2Com+fosParam[prmNum];
        httpRequest();
    }
    
    // Method to slew the camera at a preset speed
    public void moveCam()
    {
        fosCom = fos1Com+fosCmd[cmdNum]+fos2Com;
        httpRequest();
    }
    
    // Method to stop the camera
    public void stopMove()
    {
        cmdNum = 0;
        fosCom = fos1Com+fosCmd[cmdNum]+fos2Com;
        httpRequest();
    }

    // Method to reset the camera to the default position
    public void resetPosition()
    {
        cmdNum = 20;
        fosCom = fos1Com+fosCmd[cmdNum]+fos2Com;
        httpRequest();
    }
    
    // Method to send the actual HTTP request to the camera
    public void httpRequest()
    {
        try
        {
            urlSend = new Scanner( new URL(fosCom).openStream() );
        }   
        catch (MalformedURLException ex) 
        {
            Logger.getLogger(JClient.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(JClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        urlSend.close();
    }
    
    // Method to delay further action
    public void timeDelay(int timerDelay)
    {
        try 
        {
            Thread.sleep(timerDelay); 
        } 
        catch(InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
    }        
    
} // end of class JClient
