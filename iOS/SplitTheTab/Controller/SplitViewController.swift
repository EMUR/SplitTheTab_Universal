//
//  SplitViewController.swift
//  SplitTheTab
//
//  Created by Eyad on 1/11/18.
//  Copyright © 2018 Eyad. All rights reserved.
//

import UIKit
import KRAlertController
import NVActivityIndicatorView

class SplitViewController: UIViewController, UITableViewDelegate, UITableViewDataSource, UITextFieldDelegate,
CustomAmountDelegate, AddSplitterDelegate {
    @IBOutlet weak var inputSplitView: UIView!
    @IBOutlet weak var splitAmountLabel: UILabel!
    fileprivate var background:UILabel!
    @IBOutlet weak var removeSplitButton: UIButton!
    @IBOutlet var inputTextField:UITextField!
    @IBOutlet weak var splittersTableView: UITableView!
    var loadingVC:LoadingViewController!
    var customAmounts = 0
    var spliters = Array<SplitPerson>()
    var splitWith = 1
    
    
    override func viewWillAppear(_ animated: Bool) {
        if (spliters.count == 0) {
            spliters.append(SplitPerson.init(withName: "You", withShare: "0", withID: splitWith))
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        loadingVC = UIStoryboard.init(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "Loading") as! LoadingViewController
        
        let tap = UITapGestureRecognizer.init(target: self, action: #selector(splitAmountLabelClicked))
        self.splitAmountLabel.isUserInteractionEnabled = true
        self.splitAmountLabel.addGestureRecognizer(tap)
        
        self.splittersTableView.delegate = self
        self.splittersTableView.dataSource = self
     
        inputTextField.delegate = self
        inputTextField.leftViewMode = .always
        let left = UIView.init(frame: .init(origin: inputTextField.bounds.origin, size: .init(width: 40, height: inputTextField.bounds.height)))
        let dollar = UILabel.init(frame: .init(origin: CGPoint.zero, size: .init(width: 40, height: inputTextField.frame.height)))
        dollar.textColor = #colorLiteral(red: 0.3962245882, green: 0.7777633667, blue: 0.7374145389, alpha: 0.8913741438)
        dollar.text = "$"
        dollar.font = UIFont.systemFont(ofSize: 25)
        dollar.textAlignment = NSTextAlignment.center
        left.addSubview(dollar)
        inputTextField.leftView = left
        
        inputTextField.addTarget(self, action:  #selector(updateLabel), for: UIControlEvents.editingChanged)
        self.splitAmountLabel.addObserver(self, forKeyPath: "text", options: [.new], context: nil)
        self.splittersTableView.register(UINib(nibName: "SplitterViewTableViewCell", bundle: nil), forCellReuseIdentifier: "cell")
    }
    
    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        if keyPath == "text"{
            if (change?.values.first! as! String).lengthOfBytes(using: .ascii) == 2 {
                self.splitAmountLabel.text = " Enter the total amount"
                spliters.removeAll(keepingCapacity: false)
                spliters.append(SplitPerson.init(withName: "You", withShare: "0", withID: splitWith))
                updateLabel()
            }
        }
    }
    
    @objc func splitAmountLabelClicked() {
        UIView.animate(withDuration: 0.3, animations: {
            self.inputSplitView.alpha = 1.0
        }) { (done) in
            self.inputTextField.becomeFirstResponder()
            self.inputSplitView.isUserInteractionEnabled = true
        }
    }

    @IBAction func dissmissInputView(_ sender: Any) {
        self.splitAmountLabel.text = "$  " + self.inputTextField.text!
        inputTextField.resignFirstResponder()
        UIView.animate(withDuration: 0.3, animations: {
            self.inputSplitView.alpha = 0.0
        }) { (done) in
            self.inputSplitView.isUserInteractionEnabled = false
        }
    }

    
    @objc func updateLabel() {
        if self.inputTextField.text!.lengthOfBytes(using: .ascii) > 0 {
            updateSplitValues(isAdd: true, justUpdate: true)
            self.splittersTableView.reloadData()
        }
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 60
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return splitWith
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! SplitterViewTableViewCell
        
        if spliters.count > 0 && indexPath.row < spliters.count {
            cell.amount.text = "$ " + String(format:"%.2f", Float(spliters[indexPath.row].share)!)
            cell.name.text = spliters[indexPath.row].name
            cell.delegate = self
            cell.splitter_index = spliters[indexPath.row].id
        }
        
        return cell
    }
    
    @IBAction func addSplitter(_ sender: Any) {
        splitWith += 1
        updateSplitValues(isAdd: true)
        self.splittersTableView.reloadData()
    }
    
    func updateSplitValues(isAdd add:Bool!, justUpdate:Bool! = false) {
        if inputTextField == nil || Float(inputTextField.text!) == nil || Float(inputTextField.text!) == Float(0) {
            spliters.append(SplitPerson.init(withName: "Splitter \(splitWith - 1)", withShare: String(0.0), withID: splitWith))
            return
        }
        
        var newValue = Float(inputTextField.text!)!
        
        for i in 0...spliters.count - 1 {
            if spliters[i].customAmount {
                newValue = newValue - Float(spliters[i].share)!
            }
        }
        
        newValue = newValue / Float(splitWith - customAmounts)
        
        let ignoreLast = Int(truncating: NSNumber.init(booleanLiteral:!add))
        
        for i in 0...spliters.count - (1 + ignoreLast) {
            if !spliters[i].customAmount{
                spliters[i].share = String(newValue)
            }
        }
        
        if !justUpdate {
            if add {
                spliters.append(SplitPerson.init(withName: "Splitter \(splitWith - 1)", withShare: String(newValue), withID: splitWith))
                
            } else {
                if spliters[spliters.count - 2].customAmount || spliters.last!.customAmount {
                    spliters[spliters.count - 2].customAmount = false
                    let amount = Float(spliters[spliters.count - 2].share)! + Float(spliters.last!.share)!
                    spliters[spliters.count - 2].share = "\(amount)"
                }
                
                spliters.removeLast()
            }
        }
        
        if (splitWith == 1) {
            removeSplitButton.isEnabled = false
            spliters[0].share = "\(Float(inputTextField.text!)!)"
            spliters[0].customAmount = false
            customAmounts = 0
            
        } else {
            removeSplitButton.isEnabled = true
        }
    }
    
    @IBAction func removeSplitter(_ sender: Any) {
        splitWith -= 1
        
        if spliters.last!.customAmount || spliters[spliters.count - 2].customAmount {
            if spliters.last!.customAmount && spliters[spliters.count - 2].customAmount {
                customAmounts = customAmounts - 2
            
            } else {
                customAmounts = customAmounts - 1
            }
        }
        
        updateSplitValues(isAdd: false)
        self.splittersTableView.reloadData()
    }
    
    func setCusotmAmount(withAmount amount: Float, forID id:Int) {
        let str: String! = inputTextField.text?.replacingOccurrences(of: "$ ", with: "") as String!
        var newAmount = Float(str)!
        spliters[id - 1].share = "\(amount)"
        spliters[id - 1].customAmount = true
        customAmounts = 0
        
        for i in 0...spliters.count - 1 {
            if spliters[i].customAmount{
                customAmounts = customAmounts + 1
                newAmount = newAmount - Float(spliters[i].share)!
            }
        }
        
        let delta = (amount - Float(spliters[id-1].share)!)
        let payPerPerson =  newAmount / Float(splitWith - customAmounts)
        
        for i in 0...spliters.count - 1 {
            if !spliters[i].customAmount {
                spliters[i].customAmount = false
                spliters[i].share = "\(payPerPerson - delta)"
            }
        }
        
        splittersTableView.reloadData()
    }
    
    func presentCustom(withAlert alert: KRAlertController, forID id:Int) {
        alert.addAction(title: "OK", isPreferred: true) { (action, textFields) in
            let textField = textFields[0]
            self.setCusotmAmount(withAmount: Float(textField.text!)!, forID: id)
            }.textFields[0].keyboardType = UIKeyboardType.decimalPad
        
        alert.showAuthorize(icon: false)
    }
    
    func addSplitter(forID id: Int) {
        let vc = UIStoryboard.init(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "addRecentView") as! AddRecentViewController
        vc.splitter_index = id
        vc.delegate = self
        self.present(vc, animated: true)
    }
    
    @IBAction func sendRequest(_ sender: Any) {
        loadingVC.loadingText = "Sending payment request"
        self.present(loadingVC, animated: false, completion: nil)
        var owersList = [String: (amount: Double, paid: Bool)]()
        for i in 1...spliters.count-1{
            owersList[spliters[i].email.convertToDatabase()] = (Double(spliters[i].share)!,false)
        }
        var str: String! = splitAmountLabel.text?.replacingOccurrences(of: "$", with: "") as String!
        str = str.replacingOccurrences(of: " ", with: "")
        let amount = Double(str)!
        DatabaseHandler.shared().addTab(tab: Tab.init(payerID: DatabaseHandler.shared().userEmail!.convertToDatabase(), totalAmount: amount, owers: owersList))
        customAmounts = 0
        spliters = Array<SplitPerson>()
        spliters.append(SplitPerson.init(withName: "You", withShare: "0", withID: splitWith))
        splitWith = 1
        splittersTableView.reloadData()
        loadingVC.dismissView(completion: {})
    }
    
    
    func addSplitter(withEmail email: String, forID id: Int) {
        spliters[id - 1].isVerified = true
        spliters[id - 1].email = email
        var str = email
        
        if let name = str.range(of: "@") {
            str.removeSubrange(name.lowerBound..<str.endIndex)
        }
        
        spliters[id - 1].name = str
        self.splittersTableView.reloadData()
    }
}


