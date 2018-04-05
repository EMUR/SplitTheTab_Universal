# Split The Tab

Split the Tab is an iOS & Android mobile application that allows a group of people who dine together to split the cost of
the meal when one person is paying. The user can use Split the Tab both as a simple calculator, or to request and
receive payments from other users. This is achieved by allowing a user to sign up with Split the Tab, and through
the app sign up with Stripe, which provides APIs to handle peer to peer payments. 

![devices](https://i.imgur.com/BbDDg2u.png)

### Application components: <br/><br/>
------

**SPLIT VIEW** <br/>
<p align="center">
    <img src="https://i.imgur.com/DjAI1f8m.png">
</p>
This view is presented upon launching the application, which serves as the main page where a user would performs all the payment functionalities.

1) On the top section of the view is the amount text view, which is where a user would input the amount he/she originally paid for the tab. This ﬁeld only take integers and doubles for value.

2) These are the buttons where a user use to add/remove splitters to his tab, a user can add an “unbounded” number of users to split the tab with, upon clicking on the add button, a user would instantly notice the change of price in the “YOU” splitter entry.

3) This is the splitters’ list view, where a user can check all the people involved in splitting the current tab. Each list entry is referred to as a “splitter entry”, where each splitter entry consist of three major parts:

The splitter’s image, The splitter name and the amount owned by the splitter.

The ﬁrst splitter entry shows the current user’s share of the tab, as for the splitter name we choose “YOU” to be the name of the splitter to make it clear for the user that this is their share. This is the only splitter entry that is not removable by the user. For other splitter entries, we note that the splitter’s images are different and the reason is that upon adding a new splitter, we don’t have a known user associated with the given splitter entry, so we indicate to the user that an splitter account needs to be added by showing a plus sign for the splitter image.

Furthermore, we add a numbered placeholder name for each added splitter entry e.g. “Splitter #1” so we can make it easy for the user track the number of added splitters to a given tab.

The last major part on the splitter entry is the price, where upon adding a new splitter, the total amount entered for the tab will be equally divided among all the splitters (including “YOU” the user). However, the amount assigned to each splitter can be increased/decreased on increment of one by using the right and the left arrow indicators next to the owned splitter amount.

That’s said, if a user wish to changed the owned amount for any of the splitters (including them) to be a certain price, they can do so by double taping on the owned amount of a given splitter entry, which will invoke a new view which prompt the user to enter the custom amount, and once they do so, other splitter’s amounts are automatically adjusted.

So far, every splitter entry holds a generic “Splitter #” name, and an add user proﬁle image for the splitter’s proﬁle as mentioned above. To add splitter, the user tap on the add user image, which will prompt a new “splitter lookup view” that asks the user to enter the email for the user he/she wants to split the tab with, and when the user insert an email and click ﬁnd, a background database query is run to validate the existence of such splitter email. If the entered email is not in our database, the user will receive an error message and will be asked to enter another email. The “splitter lookup view” also shows a list of recently paid splitters for user’s convenience.

4) When a valid email is entered into the “splitter lookup view”, we assign the entered splitter to the associated splitter entry, and we mark the entry to be “veriﬁed” This is the Payment request button, which is where the user send payment requests to the splitters he added in the list above. If this button is pressed while having one or more unveriﬁed splitter entries, the user will receive an error message and will be asked to verify all splitter entries.
In case all splitter entries were veriﬁed, a payment request is sent to the user and to the Stripe database.
------
**PROFILE VIEW** <br/>
<p align="center">
    <img src="https://i.imgur.com/dnBif8Rm.png">
</p>
If a user decides to sign up with Split The Tab in order to use the payment
features, his or her information and payment history are displayed in the
Profile View.<br/>
1) On the top of the page, the username and email address are displayed.
The username is fixed, and is computed by extracting and capitalizing
the part of the email address before the “@“ sign.<br/>
2) A list of transactions is shown in the middle. The list can display three
kinds of lists: a list of past transactions, or history, with both ingoing and outgoing payments; a list of pending transaction owed to the current
user, which are incoming, and a list of pending transactions owed by
the user to other users.<br/>
3) The tabs at the bottom of the screen allow the user to switch between
the three lists mentioned in section 2). 
------
**LOGIN:**<br/>
<p align="center">
    <img src="https://i.imgur.com/mGyWFVlm.png">
</p>
In this page, the user gets the chance to sign-in or sign-up for Split The Tab, The processes of signing in is trivial, as the user inputs the email he/she signed up with, the password and click Sign In (2). In case the credentials existed and matched the one in the database, then the application will login and automatically dismiss the login view. Otherwise, an error message will be displayed to the user informing them with what went wrong.
<br/>
The second option is when a user needs to sign up, this is a little more elaborate as the user would have to ﬁrst input his/her preferred valid email and password and click on sign up (3). Which would then lead to another view that will ask the user to input further informations that are required in order to enable the payment feature. This includes informations like their full name, phone number, date of birth as well as their debit/credit card number for payments charging/deposits. All these details are safely stored in the Stripe database and not on our ﬁrebase database, as only a reference of their Stripe ID will be stored there. If the user entered all the required informations correctly, they will be automatically signed in, and the view will therefore be dismissed.
------
**PAYMENT VIEW**<br/>
<p align="center">
    <img src="https://i.imgur.com/bJCDQDIm.png">
</p>
If the database handler is updated with a transaction that is partly owed by the current user of the app, a popup is shown prompting the user to pay that amount. The popup contains the following information:
<br/>
1) A receipt-style alert containing the date, the email of the person that the money is owed to, and the amount.
<br/>
2) A button to deny or accept paying for that transaction.
<br/>
There is no option to pay part of the amount owed. If the user chooses to authorize the transaction, money is taken from the card associated with his or her account on Stripe, and given to the speciﬁed receiver. If the user decides not to pay, the app will remind the user periodically of that debt.
