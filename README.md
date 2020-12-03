# Open Source Java POS

## Summary

![POS_PIC](README_PICS/POS_001.PNG)

A very simple POS with basic functionality written in Java, whilst also using the TCPIP interface for JAVA from linkly. (https://github.com/pceftpos/EFTClient.IPInterface.Java) Currently there are 7 parts of this POS:

- 1-3: Options for items to add to the cart
- 4 : Receipt box that will display the receipt + the transaction response class
- 5: The cart, every time the tender button or an item button is clicked, this textbox is updated with the current cart
- 6: Displays the current total price of the cart
- 7: Starts the transaction

## Time Taken/Design Considerations



## Possible Improvements

There a larger number of improvements that can be made to this POS, even whilst still keeping it simple it nature.

### POS Improvements

- The cart display on screen could be updated to allow for the user to adjust the cart. For example being able to remove an item or to increase/decrease an item's quantity

### Linkly Integration Improvements

- Currently only purchase transaction types can be performed through the POS. Through some UI changes, the POS could be improved to handle other transaction types such as refunding.
- Only the transaction request can be sent through at the moment. A settings window could be added to allow for options for other requests like Logon, to make sure the pinpad & client are still connected.
- Linkly's software allows for basket data to be sent up before the transaction is sent up. Since this program is effectively simulating the sale of a cart, it would make sense to upgrade this POS later to add the cart to the basket data request.

## Testing



## Release Notes:

### 1.0.0.0 4/12/2020 Callum Hands

- Initial Release