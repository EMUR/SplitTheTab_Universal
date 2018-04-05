//
//  ProfileTabsTableViewCell.swift
//  SplitTheTab
//
//  Created by Eyad Murshid on 3/1/18.
//  Copyright © 2018 Eyad. All rights reserved.
//

import UIKit

class ProfileTabsTableViewCell: UITableViewCell {
    @IBOutlet weak var typeText: UILabel!
    @IBOutlet weak var typeImage: UIImageView!
    @IBOutlet weak var amount: UILabel!
    @IBOutlet weak var name: UILabel!
    
    var isRequest: Bool!
    var tabID: String?
    var tappedDelegate: ProfileCellTappedDelegate?
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func layoutSubviews() {
        if isRequest {
            typeImage.image = UIImage.init(named: "money-in")
        
        } else {
            typeImage.image = UIImage.init(named: "money-out")
            typeText.textColor = UIColor.red
            typeText.text = "- SPEND"
            amount.textColor = UIColor.red.withAlphaComponent(0.5)
            
            let tap = UITapGestureRecognizer(target: self, action: #selector(ProfileTabsTableViewCell.onClick(_:forEvent:)))
            tap.delegate = self
            self.addGestureRecognizer(tap)
        }
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
    }
    
    @objc func onClick(_ sender: Any, forEvent event: UIEvent) {
        if let delegate = tappedDelegate, let tabID = tabID {
            delegate.cellTapped(tabID: tabID, cell: self)
        }
    }
}
