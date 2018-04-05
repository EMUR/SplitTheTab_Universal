//
//  SplitterViewTableViewCell.swift
//  SplitTheTab
//
//  Created by Eyad Murshid on 2/13/18.
//  Copyright © 2018 Eyad. All rights reserved.
//

import UIKit
import KRAlertController

class SplitterViewTableViewCell: UITableViewCell {
    @IBOutlet weak var addButton: UIButton!
    @IBOutlet weak var name: UILabel!
    @IBOutlet weak var amount: UILabel!
    var splitter_index: Int!
    weak var delegate: CustomAmountDelegate!
    var notCalled = true
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func layoutSubviews() {
        if splitter_index == 1 {
            addButton.isHidden = true
        }
        
        let longtap = UILongPressGestureRecognizer.init(target: self, action: #selector(customAmount))
        amount.addGestureRecognizer(longtap)
    }
    
    @objc func customAmount(_ gestureRecognizer: UILongPressGestureRecognizer) {
         if gestureRecognizer.state == .began {
            let alert = KRAlertController.init(title: "Add Custom Pay", message: "Please Enter the new amount", style: .alert).addTextField().addCancel()
            delegate.presentCustom(withAlert: alert, forID: splitter_index)
        }
    }
    
    @IBAction func reduce(_ sender: Any) {
        let str: String! = amount.text?.replacingOccurrences(of: "$ ", with: "")
        delegate.setCusotmAmount(withAmount: Float(str)! - 1.0, forID: splitter_index)
    }
    
    @IBAction func increase(_ sender: Any) {
        let str: String! = amount.text?.replacingOccurrences(of: "$ ", with: "") as String!
        delegate.setCusotmAmount(withAmount: Float(str)! + 1.0, forID: splitter_index)
    }
    
    @IBAction func addSplitter(_ sender: Any) {
        delegate.addSplitter(forID: splitter_index)
    }
    

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
    }
    
}
