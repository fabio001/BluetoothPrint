package com.iatli.bluetoothprint;

import android.util.Log;
import com.dantsu.printerthermal_escpos_bluetooth.Printer;
import com.dantsu.printerthermal_escpos_bluetooth.bluetooth.BluetoothPrinterSocketConnection;
import com.dantsu.printerthermal_escpos_bluetooth.bluetooth.BluetoothPrinters;

public class BluetoothPrinter {
    private static final String TAG ="YENORSAN";

    private BluetoothPrinters bluetoothPrinters = null;
    private BluetoothPrinterSocketConnection[] bluetoothPrinterSocketConnections = null;
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

        Printer printer = new Printer(bluetoothPrinterSocketConnections[selectedDevice],
                203, 78f, 52);

        if(printer == null){
            return false;
        }
        printer.printFormattedText(formattedText);

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
            bluetoothPrinters = new BluetoothPrinters();
        bluetoothPrinterSocketConnections = bluetoothPrinters.getList();
        if(bluetoothPrinterSocketConnections ==null)
            return 0;
        return bluetoothPrinterSocketConnections.length;
    }
}
