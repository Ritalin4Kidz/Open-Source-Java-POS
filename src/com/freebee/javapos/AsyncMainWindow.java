package com.freebee.javapos;

import org.json.simple.parser.ParseException;
import pceft.sdk.eftclient.java.*;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class AsyncMainWindow implements PCEFTPOSEventListener, SSLSocketListener {

    //endregion
    private AsyncSocketControl AsyncCtrl;
    private JPanel Panel1;
    private JButton BurgerAddButton;
    private JButton SodaAddButton;
    private JButton ChipsAddButton;
    private JButton TenderBtn;
    private JPanel transactionPanel;
    private JTextArea ReceiptTextBox;
    private JTextArea CartTextBox;
    private JLabel ttlPriceLbl;

    CartItem chipsItem = new CartItem(5.35f, "Small Chips");
    CartItem sodaItem = new CartItem(3.50f, "Coca-Cola 600mL");
    CartItem burgerItem = new CartItem(10.75f, "Cheeseburger");

    ArrayList<CartItem> currentCart = new ArrayList<CartItem>();

    public static void main(String[] args) {
        try { ;
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");

        } catch (Exception ignored) {
        }
        JFrame frame = new JFrame("Joey's Burgers");
        frame.setContentPane(new AsyncMainWindow().Panel1);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public AsyncMainWindow() {
        TenderBtn.addActionListener(e -> DoTransaction());
        BurgerAddButton.addActionListener(e -> AddBurger());
        SodaAddButton.addActionListener(e -> AddCoke());
        ChipsAddButton.addActionListener(e -> AddChips());
        AsyncCtrl = new AsyncSocketControl(this, "127.0.0.1", 2011);
    }

    //GO THROUGH THE WHOLE CART LIST, ADJUST PRICE AND CART TEXTBOX
    private double UpdateCart() {
        //START WITH AMOUNT 0
        float TotalAmtPurchase = 0;
        //REMOVE ALL TEXT FROM CART
        CartTextBox.setText("");
        //GO THROUGH EACH ITEM IN THE CART
        for (CartItem c : currentCart
        ) {
            //ADD THE PRODUCT NAME + THE PRICE TO THE TEXTBOX, SEPARATED BY A '|'
            CartTextBox.append(c.productname + " | $" + c.price  + '\n');
            //ADD THE PRICE TO THE TOTAL
            TotalAmtPurchase += c.price;
        }
        //RETURN THE TOTAL PRICE OF THE CART (ROUNDED TO 2DP)
        return Math.round(TotalAmtPurchase * 100.0) / 100.0;
    }

    //ADDS A BURGER ITEM INTO THE CART
    private void AddBurger(){
        currentCart.add(burgerItem);
        ttlPriceLbl.setText("Total Price: $" + UpdateCart());
    }
    //ADDS A COCA-COLA ITEM INTO THE CART
    private void AddCoke(){
        currentCart.add(sodaItem);
        ttlPriceLbl.setText("Total Price: $" + UpdateCart());
    }
    //ADDS THE CHIPS ITEM INTO THE CART
    private void AddChips(){
        currentCart.add(chipsItem);
        ttlPriceLbl.setText("Total Price: $" + UpdateCart());
    }

    private double SaveTransaction(String txnReference) throws IOException, ParseException {
        //INITIALISE TOTAL AMOUNT AND TOTAL QUANTITY VALUES
        double totalAmtPurchase = 0;
        int totalAmtQuantity = 0;
        //CREATE NEW JSON OBJECTS/ARRAYS FOR STORING
        JSONObject txnStored = new JSONObject();
        JSONArray txnCart = new JSONArray();
        //GO THROUGH EACH ITEM IN THE CART, ADJUST VALUES FOR TOTAL AMOUNT,QUANTITY & CART
        for (CartItem c : currentCart
        ) {
            txnCart.put(c.toString());
            totalAmtQuantity++;
            totalAmtPurchase += c.price;
        }
        //STORE THE ARRAY INTO THE JSON OBJECT
        txnStored.put(txnReference,txnCart);
        //STORE THE TOTAL AMOUNT OF PURCHASE IN THE JSON OBJECT
        txnStored.put("Total Price", totalAmtPurchase);
        //STORE THE TOTAL AMOUNT OF ITEMS IN THE JSON OBJECT
        txnStored.put("Total Items", totalAmtQuantity);
        //GET THE CURRENT DAY OF THE YEAR SO THIS CAN BE STORED IN FILES NAMED BY THE DATE
        LocalDateTime ldt = LocalDateTime.now();
        String fileStr = "TransactionStored_" + ldt.getDayOfMonth() + "_" + ldt.getMonth() + "_" + ldt.getYear() + ".json";
        //GET THE CURRENT DATA IN THE DAYS STORED TRANSACTIONS
        String fileContents = "";
        File f = new File(fileStr);
        //IF THE FILE EXISTS
        if(f.isFile()) {
            fileContents = new String ( Files.readAllBytes( Paths.get(fileStr) ) );
        }
        FileWriter file;
        file = new FileWriter(fileStr);
        //ADD ON THE NEW TRANSACTION TO THE FILE
        file.write(fileContents + "\n" + txnStored.toString(1));
        //SAVE AND CLOSE THE FILE
        file.flush();
        file.close();
        //CLEAR THE CART AND UPDATE THE SCREEN
        currentCart.clear();
        ttlPriceLbl.setText("Total Price: $" + UpdateCart());
        //RETURN THE TOTAL AMOUNT OF PURCHASE FOR USE IN LINKLY TRANSACTION
        return totalAmtPurchase;
    }

    //DO TRANSACTION WILL ACTUALLY MAKE A CALL TO THE LINKLY CLIENT IF CONNECTED, ELSE IT WILL JUST IGNORE
    private void DoTransaction(){
        try {
            //CREATE A NEW TXN REQUEST OBJECT
            EFTTransactionRequest Txn = new EFTTransactionRequest();
            //STORE THE TRANSACTION TO A FILE AND GET THE TOTAL AMOUNT AS AMOUNT PURCHASE
            Txn.AmtPurchase = SaveTransaction("270220201003333 ");
            //IN THIS CASE THERE IS NO AMOUNT CASH IN THE TXN, HOWEVER CAN BE ADJUSTED AT WILL
            Txn.AmtCash = 0.00;
            //SET AS 00 AND EFTPOS FOR EFTPOS PAYMENT
            Txn.Merchant = "00";
            Txn.Application = EFTTransactionRequest.TerminalApplication.EFTPOS;
            //PURCHASE TXN TYPE
            Txn.TxnType = EFTTransactionRequest.TransactionType.PurchaseCash;
            Txn.TxnRef = "270220201003333 ";
            Txn.BankDate = Date.from(Instant.now());
            //Txn.PurchaseAnalysisData = PADTxtBox.getText();
            if (AsyncCtrl.socket.isConnected()) {
                try {
                    AsyncCtrl.socketSend(Txn);
                } catch (Exception e) {
                    //textArea1.setText(String.format("Socket write failed with the following exception: %s %s", e.toString(), e.getMessage()));
                    System.out.println("MSG: " + e.getMessage());
                    System.out.println("Cause: " + e.getCause());
                    System.out.println("RAW: " + e.toString());
                }
            }
            else
            {
                ReceiptTextBox.setText("No EFT-Client Found. Txn Stored But Not Continued");
            }
        }
        catch (Exception e){
            System.out.println("MSG: " + e.getMessage());
            System.out.println("Cause: " + e.getCause());
            System.out.println("RAW: " + e.toString());
        }
    }

    @Override
    public void onLogonEvent(EFTLogonResponse eftLogonResponse) {

    }

    @Override
    public void onReceiptEvent(EFTReceiptResponse eftReceiptResponse) {
        //CLEAR THE RECEIPT TEXT
        ReceiptTextBox.setText("");
        if (eftReceiptResponse.Receipt.Receipt == 'R')
        {
            for (String s : eftReceiptResponse.ReceiptText
            ) {
                //ADD THE RECEIPT TO THE RECEIPTTEXTBOX TO DISPLAY
                ReceiptTextBox.append(s + '\n');
            }
        }
        //TELL THE EFT-CLIENT THAT WE HAVE SUCCESSFULLY RECEIVED THE RECEIPT
        try {
            AsyncCtrl.socketSend(new EFTReceiptRequest());
        }
        catch (Exception e){
            System.out.println("MSG: " + e.getMessage());
            System.out.println("Cause: " + e.getCause());
            System.out.println("RAW: " + e.toString());
        }
    }

    @Override
    public void onTransactionEvent(EFTTransactionResponse eftTransactionResponse) {
        //ADD THE RESPONSE OBJECT TO THE TEXTBOX FOR VIEWING PURPOSES
        ReceiptTextBox.append("\n");
        ReceiptTextBox.append(eftTransactionResponse.getClass().toString());
        JSONObject json = new JSONObject(eftTransactionResponse);
        ReceiptTextBox.append(json.toString(1));
    }

    @Override
    public void onDisplayEvent(EFTDisplayResponse eftDisplayResponse) {

    }

    @Override
    public void onStatusEvent(EFTStatusResponse eftStatusResponse) {

    }

    @Override
    public void onSettlementEvent(EFTSettlementResponse eftSettlementResponse) {

    }

    @Override
    public void onGetLastTransactionEvent(EFTGetLastTransactionResponse eftGetLastTransactionResponse) {

    }

    @Override
    public void onReprintReceiptEvent(EFTReprintReceiptResponse eftReprintReceiptResponse) {

    }

    @Override
    public void onControlPanelEvent(EFTControlPanelResponse eftControlPanelResponse) {

    }

    @Override
    public void onSetDialogEvent(EFTSetDialogResponse eftSetDialogResponse) {

    }

    @Override
    public void onPinpadBusyEvent(EFTPinpadBusyResponse eftPinpadBusyResponse) {

    }

    @Override
    public void onQueryCardEvent(EFTQueryCardResponse eftQueryCardResponse) {

    }

    @Override
    public void onGenericCommandEvent(EFTGenericCommandResponse eftGenericCommandResponse) {

    }

    @Override
    public void onClientListEvent(EFTClientListResponse eftClientListResponse) {

    }

    @Override
    public void onChequeAuthEvent(EFTChequeAuthResponse eftChequeAuthResponse) {

    }

    @Override
    public void onCloudPairEvent(EFTCloudPairResponse eftCloudPairResponse) {

    }

    @Override
    public void onCloudTokenLogonEvent(EFTCloudTokenLogonResponse eftCloudTokenLogonResponse) {

    }

    @Override
    public void onDefaultResponseEvent(String s) {

    }

    @Override
    public void onDisconnect() {

    }
}
