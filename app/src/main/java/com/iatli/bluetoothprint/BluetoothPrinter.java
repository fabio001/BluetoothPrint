package com.iatli.bluetoothprint;

import android.util.Log;

import com.dantsu.escposprinter.EscPosCharsetEncoding;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;

public class BluetoothPrinter {
    private static final String TAG ="YENORSAN";

    private BluetoothPrintersConnections bluetoothPrinters = null;
    private BluetoothConnection[] bluetoothConnections = null;
    private int selectedDevice;

    public BluetoothPrinter(){
        selectedDevice = 0;
    }

    public boolean printOnDevice(String formattedText){
        Log.d(TAG, "Bluetooth printer function is called.");
        int numDevices = getBluetoothPrinterCount();

        if(numDevices<=0)
            return false;

        //somehow device get lost connection is lost one of them. Thus it will get first one.
        if(selectedDevice>=numDevices){
            selectedDevice=0;
        }

        EscPosPrinter printer = null;
        try {
            printer = new EscPosPrinter(bluetoothConnections[selectedDevice],
                    203, 78f, 52,
                    new EscPosCharsetEncoding("windows-1254", 16));

            printer.printFormattedText(formattedText);
        } catch (EscPosConnectionException e) {
            Log.d(TAG, e.getLocalizedMessage());
        } catch (EscPosBarcodeException e) {
            Log.d(TAG, e.getLocalizedMessage());
        } catch (EscPosEncodingException e) {
            Log.d(TAG, e.getLocalizedMessage());
        } catch (EscPosParserException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }


        if(printer == null){
            return false;
        }

        Log.d(TAG, "printing function done");
        return true;
    }

    public void setSelectedDevice(int selectedDevice){
        int numOfDevice= getBluetoothPrinterCount();
        if(selectedDevice < numOfDevice){
            this.selectedDevice = selectedDevice;
        }
    }

    public int getBluetoothPrinterCount(){
        if(bluetoothPrinters == null)
            bluetoothPrinters = new BluetoothPrintersConnections();
        bluetoothConnections = bluetoothPrinters.getList();
        if(bluetoothConnections ==null)
            return 0;
        return bluetoothConnections.length;
    }
}
