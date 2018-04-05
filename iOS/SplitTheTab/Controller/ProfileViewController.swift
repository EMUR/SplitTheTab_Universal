//
//  ProfileViewController.swift
//  SplitTheTab
//
//  Created by Eyad Murshid on 1/20/18.
//  Copyright © 2018 Eyad. All rights reserved.
//

import UIKit
import Firebase

class ProfileViewController: UIViewController, UITableViewDelegate, UITableViewDataSource, ReloadDelegate, ProfileCellTappedDelegate {
    @IBOutlet weak var signActionButton: UIButton!
    @IBOutlet weak var owingTable: UITableView!
    @IBOutlet weak var owedToTable: UITableView!
    @IBOutlet weak var username: UILabel!
    var owers = Array<(String, Double, Bool, String)>()
    var owing = Array<(String, Double, Bool, String)>()
    @IBOutlet weak var loginSignupB: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        owedToTable.delegate = self
        owedToTable.dataSource = self
        
        owingTable.delegate = self
        owingTable.dataSource = self
        self.owedToTable.register(UINib(nibName: "ProfileTabsTableViewCell", bundle: nil), forCellReuseIdentifier: "cell")
        self.owingTable.register(UINib(nibName: "ProfileTabsTableViewCell", bundle: nil), forCellReuseIdentifier: "cell")
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if tableView == owedToTable {
            return owers.count
            
        } else {
            return owing.count
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell: ProfileTabsTableViewCell!
        let amount: Double
        
        if tableView == owedToTable {
            cell = owedToTable.dequeueReusableCell(withIdentifier: "cell") as! ProfileTabsTableViewCell
            cell.isRequest = true
            amount = owers[indexPath.row].1
            cell.name.text = "\(owers[indexPath.row].0)"
            
        } else {
            cell = owingTable.dequeueReusableCell(withIdentifier: "cell") as! ProfileTabsTableViewCell
            cell.isRequest = false
            amount = owing[indexPath.row].1
            cell.name.text = "\(owing[indexPath.row].0)"
            cell.tappedDelegate = self
            cell.tabID = owing[indexPath.row].3
        }
        
        var stringAmount = "$\(amount)"
        
        switch Decimal(amount).significantFractionalDecimalDigits {
        case let decimalPlaces where decimalPlaces < 2:
            stringAmount += "0"
        case 2:
            break
        default:
            stringAmount = "$\(amount.truncate(places: 2))"
        }
        
        cell.amount.text = stringAmount
        
        return cell
    }
    
    func reloadTable(owingOrOwed: ReloadDelegateOptions) {
        switch owingOrOwed {
        case .owed:
            reloadOwersTable()
        case .owing:
            reloadOwingTable()
        default:
            reloadTables()
        }
    }
    
    func reloadTables() {
        reloadOwingTable()
        reloadOwersTable()
    }
    
    func reloadOwingTable() {
        getOwing()
        owingTable.reloadData()
    }
    
    func reloadOwersTable() {
        getOwers()
        owedToTable.reloadData()
    }
    
    func getOwers() {
        let previousCount = owers.count
        
        owers.removeAll()
        
        for tab in DatabaseHandler.shared().tabsOwedTo {
            for key in tab.owers.keys{
                owers.append((key,(tab.owers[key]?.amount)!, (tab.owers[key]?.paid)!, (tab.tabID)!))
            }
        }
        
        if owers.count == previousCount - 1 {
            let alert = UIAlertController(title: "You got paid!", message: "Someone who owed you money paid you.", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Ok", style: .cancel, handler: nil))
            self.present(alert, animated: true)
        }
    }
    
    func getOwing() {
        let previousCount = owing.count
        
        owing.removeAll()
        
        let username = DatabaseHandler.shared().userEmail
        
        for tab in DatabaseHandler.shared().tabsOwing {
            owing.append((tab.payerID,(tab.owers[username!]?.amount)!,(tab.owers[username!]?.paid)!, (tab.tabID)!))
        }
        
        if owing.count == previousCount - 1 {
            let alert = UIAlertController(title: "Success!", message: "Your payment was successful.", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Ok", style: .cancel, handler: nil))
            self.present(alert, animated: true)
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        if Auth.auth().currentUser == nil{
            loginViewHidden(isHidden: false)
        
        } else {
            loginViewHidden(isHidden: true)
        }
    }
    
    override func viewDidAppear(_ animated: Bool) {
        if Auth.auth().currentUser != nil {
            loginViewHidden(isHidden: true)
            let userEmail = Auth.auth().currentUser!.email!
            _ = DatabaseHandler.shared().reloadDelegate = self
            
            var userName = userEmail
            
            if let name = userEmail.range(of: "@") {
                userName.removeSubrange(name.lowerBound..<userName.endIndex)
            }
            
            self.username.text = userName
            reloadTables()
            
        } else {
            loginViewHidden(isHidden: false)
        }
    }
    
    func loginViewHidden(isHidden hidden:Bool){
        if hidden {
            UIView.animate(withDuration: 0.2, animations: {
                self.loginSignupB.alpha = 0.01
            }, completion: { (_) in
                self.loginSignupB.isHidden = true
            })
            
        } else {
            loginSignupB.isHidden = false
            
            UIView.animate(withDuration: 0.2, animations: {
                self.loginSignupB.alpha = 1.0
            }, completion: { (_) in
            })
        }
    }
    
    private func displayLogin() {
        let vc = UIStoryboard.init(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "rootNavigation") as! UINavigationController
        self.present(vc, animated: true, completion: nil)
    }
    
    @IBAction func loginSignupAction(_ sender: Any) {
        displayLogin()
    }
    
    @IBAction func signInButtonPressed(_ sender: UIButton) {
        if Auth.auth().currentUser != nil {
            do {
                try Auth.auth().signOut()
                owers.removeAll(keepingCapacity: false)
                owing.removeAll(keepingCapacity: false)
                owingTable.reloadData()
                owedToTable.reloadData()
                loginViewHidden(isHidden: false)
                
            } catch {
                print("Error")
            }
        }
    }
    
    func cellTapped(tabID: String, cell: ProfileTabsTableViewCell) {
        let amount = cell.amount.text!

        let originalTab = DatabaseHandler.shared().tabsOwing.filter { (tab) -> Bool in
            return tab.tabID! == tabID
            }[0]
        
        let exactAmount = originalTab.owers.filter { (ower) -> Bool in
            return ower.key == DatabaseHandler.shared().userEmail!
            }[0].value.amount
        
        let alert = UIAlertController(title: "Send payment?", message: "Would you like to pay \(originalTab.payerID) \(amount)?", preferredStyle: .alert)
        
        alert.addAction(UIAlertAction(title: "Yes", style: .default, handler: { (action) in
            DatabaseHandler.shared().payClient(withTabId: tabID, withEmail: cell.name.text!, withAmount: exactAmount)
        }))
        
        alert.addAction(UIAlertAction(title: "No", style: .cancel, handler: nil))
        
        self.present(alert, animated: true)
    }
}
