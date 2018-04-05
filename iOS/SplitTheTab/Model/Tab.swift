//
//  Tab.swift
//  SplitTheTab
//
//  Created by Andrea Vitek on 1/30/18.
//  Copyright © 2018 Eyad. All rights reserved.
//

import Foundation

class Tab: CustomStringConvertible, Equatable {
    var tabID: String?
    var payerID: String
    var totalAmount: Double
    var owers: [String: (amount: Double, paid: Bool)]
    public var description: String {
        var finalDescription = ""
        
        if let id = tabID {
            finalDescription += "ID: \(id). "
        }
        
        finalDescription += "payerID: \(payerID), totalAmount: $\(totalAmount). Owers: "
        
        for (id, amountAndPaid) in owers {
            finalDescription += "\(id): $\(amountAndPaid.amount)"
            
            if (amountAndPaid.paid) {
                finalDescription += ", paid."
            } else {
                finalDescription += ", not paid."
            }
        }
        
        return finalDescription
    }
    
    static func == (lhs: Tab, rhs: Tab) -> Bool {
        return lhs.tabID == rhs.tabID
    }
    
    convenience init(payerID: String, totalAmount: Double, owers: [String: (amount: Double, paid: Bool)]) {
        self.init(tabID: nil, payerID: payerID, totalAmount: totalAmount, owers: owers)
    }
    
    init(tabID: String?, payerID: String, totalAmount: Double, owers: [String: (amount: Double, paid: Bool)]) {
        self.tabID = tabID
        self.payerID = payerID
        self.totalAmount = totalAmount
        self.owers = owers
    }
    
    func addOwer(id: String, amount: Double, paid: Bool) {
        owers[id] = (amount: amount, paid: paid)
    }
    
    func getOwersAsList() -> [String] {
        return Array(owers.keys)
    }
    
    func getOwersForDatabase() -> NSMutableDictionary {
        let convertedOwers: NSMutableDictionary = [:]
        
        for (id, amountAndPaid) in owers {
            let amountAndPaidDict : NSMutableDictionary = [:]
            amountAndPaidDict.setValue(amountAndPaid.amount, forKey: "amount")
            amountAndPaidDict.setValue(amountAndPaid.paid, forKey: "paid")
            convertedOwers.setValue(amountAndPaidDict, forKey: id.convertToDatabase())
        }
        
        return convertedOwers
    }
}
