package com.freebee.javapos;

import pceft.sdk.eftclient.java.*;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    CartItem chipsItem = new CartItem(5.35f,1, "Small Chips");
    CartItem sodaItem = new CartItem(3.50f,1, "Coca-Cola 600mL");
    CartItem burgerItem = new CartItem(10.75f,1, "Cheeseburger");

    ArrayList<CartItem> currentCart = new ArrayList<CartItem>();

    public static void main(String[] args) {
        try { ;
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");

        } catch (Exception ignored) {
        }
        JFrame frame = new JFrame("Simple Java POS");
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

    private void UpdateCart() {
        CartTextBox.setText("");
        for (CartItem c : currentCart
        ) {
            CartTextBox.append(c.productname + " | $" + c.price  + '\n');
        }
    }

    private void AddBurger(){
        currentCart.add(burgerItem);
        UpdateCart();
    }
    private void AddCoke(){
        currentCart.add(sodaItem);
        UpdateCart();
    }
    private void AddChips(){
        currentCart.add(chipsItem);
        UpdateCart();
    }

    private float SaveTransaction(){
        float TotalAmt = 0;
        for (CartItem c : currentCart
        ) {
            TotalAmt += c.price;
        }
        currentCart.clear();
        return TotalAmt;
    }

    private void DoTransaction() {
        EFTTransactionRequest Txn = new EFTTransactionRequest();
        Txn.AmtPurchase = SaveTransaction();
        Txn.AmtCash = 0.00;
        Txn.Merchant = "00";
        Txn.Application = EFTTransactionRequest.TerminalApplication.EFTPOS;
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
    }

    @Override
    public void onLogonEvent(EFTLogonResponse eftLogonResponse) {

    }

    @Override
    public void onReceiptEvent(EFTReceiptResponse eftReceiptResponse) {
        ReceiptTextBox.setText("");
        if (eftReceiptResponse.Receipt.Receipt == 'R')
        {
            for (String s : eftReceiptResponse.ReceiptText
            ) {
                ReceiptTextBox.append(s + '\n');
            }
        }
        try {
            AsyncCtrl.socketSend(new EFTReceiptRequest());
        }
        catch (Exception e){

        }
    }

    @Override
    public void onTransactionEvent(EFTTransactionResponse eftTransactionResponse) {
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
