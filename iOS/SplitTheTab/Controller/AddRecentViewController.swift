//
//  AddRecentViewController.swift
//  SplitTheTab
//
//  Created by Eyad Murshid on 3/3/18.
//  Copyright © 2018 Eyad. All rights reserved.
//

import UIKit
import KRAlertController

class AddRecentViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return DatabaseHandler.shared().recentlyUsedPayerEmails.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = recentTable.dequeueReusableCell(withIdentifier: "cell")
        
        let index = DatabaseHandler.shared().recentlyUsedPayerEmails.index(DatabaseHandler.shared().recentlyUsedPayerEmails.startIndex, offsetBy: indexPath.row)

        cell?.textLabel?.textColor = #colorLiteral(red: 0.3177971244, green: 0.587005794, blue: 0.6434370279, alpha: 1)
        cell?.textLabel?.text = "⧁ \(DatabaseHandler.shared().recentlyUsedPayerEmails[index])"
        
        return cell!
    }
    
    @IBOutlet weak var recentTable: UITableView!
    var delegate:AddSplitterDelegate! = nil
    @IBOutlet weak var emailToAdd: UITextField!
    var splitter_index: Int!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.viewWithTag(2)?.layer.borderColor = #colorLiteral(red: 0.8039215803, green: 0.8039215803, blue: 0.8039215803, alpha: 1)
        self.view.viewWithTag(2)?.layer.borderWidth = 4.0
       
        recentTable.delegate = self
        recentTable.dataSource = self
    }
    
    override func viewWillAppear(_ animated: Bool) {
        UIView.animate(withDuration: 0.3, delay: 0.3, options: .curveEaseIn, animations: {
            self.view.backgroundColor = UIColor.black.withAlphaComponent(0.3)
        }, completion: nil)
    }
    
 
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let str = (tableView.cellForRow(at: indexPath)!.textLabel?.text)!
        delegate.addSplitter(withEmail: str.substring(from: str.index(str.startIndex, offsetBy: 2)), forID: splitter_index)
        self.dismiss(animated: true, completion: nil)
    }
    
    @IBAction func add(_ sender: Any) {
        DatabaseHandler.shared().addRecentlyPaidEmail(email: emailToAdd.text!) { (success) in
            if success {
                self.delegate.addSplitter(withEmail: self.emailToAdd.text!, forID: self.splitter_index)
                self.dismiss(animated: true, completion: nil)
            
            } else {
                KRAlertController(title: "Invalid Username", message: "The username entered doesn't exist in the database")
                    .addCancel()
                    .showError(icon: false)
            }
        }
    }
    
    @IBAction func cancel(_ sender: Any) {
        self.view.backgroundColor = UIColor.clear
        self.dismiss(animated: true, completion: nil)
    }
}
