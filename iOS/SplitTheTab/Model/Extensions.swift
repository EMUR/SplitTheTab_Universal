//
//  Extensions.swift
//  SplitTheTab
//
//  Created by Andrea Vitek on 3/6/18.
//  Copyright © 2018 Eyad. All rights reserved.
//

import Foundation

extension Decimal {
    var significantFractionalDecimalDigits: Int {
        return max(-exponent, 0)
    }
}

extension Double {
    func truncate(places : Int)-> Double {
        return Double(floor(pow(10.0, Double(places)) * self)/pow(10.0, Double(places)))
    }
}

extension String {
    func convertToDatabase() -> String {
        return self.replacingOccurrences(of: ".", with: "~")
    }
    
    func convertBackFromDatabase() -> String {
        return self.replacingOccurrences(of: "~", with: ".")
    }
}
