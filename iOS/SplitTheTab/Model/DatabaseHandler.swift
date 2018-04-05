//
//  DatabaseHandler.swift
//  SplitTheTab
//
//  Created by Andrea Vitek on 1/23/18.
//  Copyright © 2018 Eyad. All rights reserved.
//

import UIKit
import Firebase
import FirebaseDatabase
import FirebaseAuth
import Alamofire

enum ReloadDelegateOptions {
    case owing, owed, both
}

class DatabaseHandler {
    private var ref: DatabaseReference?
    private let tabsDatabaseReferenceString = "tabs_database"
    private let stripeDatabaseReferenceString = "stripe_customers"
    public var userEmail: String?
    public var stripeUserID: String?
    public var tabsOwedTo = [Tab]()
    public var tabsOwing = [Tab]()
    private var tabsHistory = [Tab]()
    public var recentlyUsedPayerEmails = Set<String>()
    var reloadDelegate: ReloadDelegate?
    
    private static var sharedDatabaseHandler = DatabaseHandler()
    
    private init() {
        if ref == nil {
            ref = Database.database().reference()
            
            if let email = Auth.auth().currentUser?.email {
                userEmail = email
            }
            
            startListening()
        }
    }
    
    func resetDatabase() {
        DatabaseHandler.sharedDatabaseHandler = DatabaseHandler()
    }
    
    private func startListening() {
        setupOwedToListener()
        setupOwingListener()
        setupStripeIDListener()
        setupRecentlyPaidEmailsListener()
    }
    
    class func shared() -> DatabaseHandler {
        return sharedDatabaseHandler
    }
    
    func userIsLoggedIn() -> Bool {
        return Auth.auth().currentUser != nil
    }
    
    func addTab(tab: Tab) {
        self.ref!.child("tabs_database").childByAutoId().setValue(["payerID": tab.payerID, "totalAmount": tab.totalAmount, "owers": tab.getOwersForDatabase()])
    }
    
    private func setupOwedToListener() {
        let query = Database.database().reference(withPath: tabsDatabaseReferenceString).queryOrdered(byChild: "payerID").queryEqual(toValue: userEmail!.convertToDatabase())
        
        query.observe(DataEventType.childAdded) { (data) in
            self.modifyOwed(snapshot: data)
        }
        
        query.observe(DataEventType.childChanged) { (data) in
            self.modifyOwed(snapshot: data)
        }
    }
    
    private func setupOwingListener() {
        let query = Database.database().reference(withPath: tabsDatabaseReferenceString).queryOrdered(byChild: "owers/" + userEmail!.convertToDatabase()).queryStarting(atValue: 0)
        
        query.observe(DataEventType.childAdded) { (data) in
            self.modifyOwing(snapshot: data)
        }
        
        query.observe(DataEventType.childChanged) { (data) in
            self.modifyOwing(snapshot: data)
        }
    }
    
    private func setupStripeIDListener() {
        if let currentUserEmail = Auth.auth().currentUser?.email {
            ref!.child(stripeDatabaseReferenceString).child(currentUserEmail.convertToDatabase()).observeSingleEvent(of: .value, with: { (snapshot) in
                let value = snapshot.value as? NSDictionary
                self.stripeUserID = value?["stripe_id"] as? String ?? ""
                
            }) { (error) in
                print(error.localizedDescription)
            }
            
        } else {
            print("No current user")
        }
    }
    
    public func getStripeID(ForUser user:String!) -> String{
    
    return "acct_1BUQuPB6dmTXtDb3"
    }
    
    private func setupRecentlyPaidEmailsListener() {
        if let currentUserEmail = Auth.auth().currentUser?.email {
            ref!.child(stripeDatabaseReferenceString).child(currentUserEmail.convertToDatabase()).observeSingleEvent(of: .value, with: { (snapshot) in
                let value = snapshot.value as? NSDictionary
                let recentlyUsedEmailsDict = value? ["recently_paid"] as? NSMutableDictionary ?? [:]
                
                for key in recentlyUsedEmailsDict.allKeys {
                    self.recentlyUsedPayerEmails.insert((key as! String).convertBackFromDatabase())
                }
                
            }) { (error) in
                print(error.localizedDescription)
            }
        } else {
            print("No current user")
        }
    }
    
    func addRecentlyPaidEmail(email: String, completion : @escaping (Bool) -> ()) {
        if let currentUserEmail = Auth.auth().currentUser?.email {
            
            let query = Database.database().reference(withPath: stripeDatabaseReferenceString).queryOrderedByKey().queryEqual(toValue: email.convertToDatabase())
            
            query.observeSingleEvent(of: DataEventType.value, with: { (snapshot) in
                guard snapshot.exists() else{
                    completion(false)
                    return
                }
                
                self.recentlyUsedPayerEmails.insert(email)
                let emailDict: NSMutableDictionary = [:]
                
                for thisEmail in self.recentlyUsedPayerEmails {
                    emailDict.setValue(true, forKey: thisEmail.convertToDatabase())
                }
                
                self.ref!.child(self.stripeDatabaseReferenceString).child(currentUserEmail.convertToDatabase()).child("recently_paid").setValue(emailDict)
                completion(true)
            })
            
        } else {
            print("No current user")
            completion(false)
        }
    }
    
    func markPartOfTabPaid(tabID: String, owerID: String) {
        let updateQuery = ref!.child("tabs_database").child(tabID).child("owers").child(owerID.convertToDatabase())
        let childUpdates = ["paid": true]
        updateQuery.updateChildValues(childUpdates)
    }
    
    private func modifyOwed(snapshot: DataSnapshot) {
        print("Modify owed called")
        let tabID = snapshot.key
        
        var totalAmount: Double!
        var owersEmails = Array<String>()
        var owersTabs = Array<(Double, Bool)>()
        
        let tabAlreadyExists = self.tabsOwedTo.filter { (tab) -> Bool in
            return tab.tabID! == tabID
        }
        
        let snaps = snapshot.children.allObjects
        
        for snap in snaps {
            if (snap as! DataSnapshot).key == "totalAmount" {
                totalAmount = Double.init(exactly: (snap as! DataSnapshot).value! as! NSNumber)
            }
            
            if (snap as! DataSnapshot).key == "owers" {
                let info = (snap as! DataSnapshot).value! as! NSDictionary
                
                for key in info.allKeys {
                    let paid = (((info.value(forKey: key as! String)!) as! NSDictionary)["paid"]! as! Bool)
                    
                    if !paid {
                        owersEmails.append((key as! String).convertBackFromDatabase())
                        let amount = (((info.value(forKey: key as! String)!) as! NSDictionary)["amount"]!)
                        owersTabs.append((Double(truncating: (amount as! NSNumber)), paid))
                    }
                }
            }
        }
        
        if !owersEmails.isEmpty {
            var owers = [String: (amount: Double, paid: Bool)]()
            
            for i in 0...owersEmails.count - 1 {
                owers.updateValue(owersTabs[i], forKey: owersEmails[i])
            }
            
            let newTab = Tab(tabID: tabID, payerID: (self.userEmail?.convertToDatabase())!, totalAmount: Double(totalAmount), owers: owers)
            
            if tabAlreadyExists.isEmpty {
                // Create new tab
                self.tabsOwedTo.append(newTab)
                
            } else {
                // Update existing tab
                let existingIndex = self.tabsOwedTo.index(of: tabAlreadyExists[0])!
                self.tabsOwedTo[existingIndex] = newTab
            }
            
            reloadTableDelegate(owingOrOwed: .owed)
            
            // No more owers, but tab still exists
        } else if !tabAlreadyExists.isEmpty {
            let existingIndex = self.tabsOwedTo.index(of: tabAlreadyExists[0])!
            self.tabsOwedTo.remove(at: existingIndex)
            reloadTableDelegate(owingOrOwed: .owed)
        }
    }
    
    public func addUserWithStripeAccount(StripeAccount acc:String!){
        if let currentUserEmail = Auth.auth().currentUser?.email {
            _ = Database.database().reference(withPath: stripeDatabaseReferenceString).child(currentUserEmail.convertToDatabase()).setValue(["stripe_id":acc])
        }
    }
    
    private func modifyOwing(snapshot: DataSnapshot) {
        print("Modify owing called")
        let tabID = snapshot.key
        
        var totalAmount: Float!
        var owingEmails = Array<String>()
        var owingTabs = Array<(Double, Bool)>()
        
        let tabAlreadyExists = self.tabsOwing.filter { (tab) -> Bool in
            return tab.tabID == tabID
        }
        
        let snaps = snapshot.children.allObjects
        var payerID = ""
        
        for snap in snaps {
            if (snap as! DataSnapshot).key == "totalAmount" {
                totalAmount = (snap as! DataSnapshot).value! as! Float
            }
            
            if (snap as! DataSnapshot).key == "payerID" {
                payerID = (snap as! DataSnapshot).value! as! String
            }
            
            if (snap as! DataSnapshot).key == "owers" {
                let info = (snap as! DataSnapshot).value! as! NSDictionary
                
                for key in info.allKeys {
                    // Only add tab if the current users portion was unpaid
                    if (key as! String).convertBackFromDatabase() == self.userEmail! {
                        let paid = (((info.value(forKey: key as! String)!) as! NSDictionary)["paid"]! as! Bool)
                        
                        if !paid {
                            owingEmails.append((key as! String).convertBackFromDatabase())
                            let amount = (((info.value(forKey: key as! String)!) as! NSDictionary)["amount"]!)
                            owingTabs.append((Double(truncating: (amount as! NSNumber)), paid))
                        }
                    }
                }
            }
        }
        
        if !owingEmails.isEmpty {
            var owers = [String: (amount: Double, paid: Bool)]()
            
            for i in 0...owingEmails.count - 1 {
                owers.updateValue(owingTabs[i], forKey: owingEmails[i])
            }
            
            let newTab = Tab(tabID: tabID, payerID: payerID.convertBackFromDatabase(), totalAmount: Double(totalAmount), owers: owers)
            
            if tabAlreadyExists.isEmpty {
                // Create new tab
                self.tabsOwing.append(newTab)
                
            } else {
                // Update existing tab
                let existingIndex = self.tabsOwing.index(of: tabAlreadyExists[0])!
                self.tabsOwing[existingIndex] = newTab
            }
            
            print(self.tabsOwing)
            
            reloadTableDelegate(owingOrOwed: .owing)
            
            // User is no longer an ower, but tab still exists
        } else if !tabAlreadyExists.isEmpty {
            let existingIndex = self.tabsOwing.index(of: tabAlreadyExists[0])!
            self.tabsOwing.remove(at: existingIndex)
            print(self.tabsOwing)
            reloadTableDelegate(owingOrOwed: .owing)
        }
    }
    
    private func reloadTableDelegate(owingOrOwed: ReloadDelegateOptions) {
        if let delegate = self.reloadDelegate {
            delegate.reloadTable(owingOrOwed: owingOrOwed)
        }
    }
    
    public func payClient(withTabId tabID: String, withEmail email: String!, withAmount amount: Double!){
        let secret = "Basic " + ("sk_test_hHnZqkTRUgfhd3sRbMpZ4UX7:".data(using: String.Encoding.utf8)?.base64EncodedString())!
        let parameter_s = ["amount":"\(Int(amount * 100))","currency":"usd","source":"tok_mastercard"]
        let header_s = ["Content-Type":"application/x-www-form-urlencoded", "Authorization":"\(secret)","Stripe-Account":"\(String(describing: self.stripeUserID!))"]

        Alamofire.request(URL.init(string: "https://api.stripe.com/v1/charges")!, method: HTTPMethod.post, parameters: parameter_s, encoding: URLEncoding.default, headers: header_s).responseJSON { (data) in
            self.markPartOfTabPaid(tabID: tabID, owerID: self.userEmail!.convertToDatabase())
        }
    }
}
