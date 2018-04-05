//
//  SplitPerson.swift
//  SplitTheTab
//
//  Created by Eyad Murshid on 2/14/18.
//  Copyright © 2018 Eyad. All rights reserved.
//

import UIKit

class SplitPerson: NSObject {
    var name: String!
    var share: String!
    var id: Int!
    var customAmount = false
    var isVerified = false
    var email:String!

    init(withName name: String, withShare share: String, withID id: Int) {
        super.init()
        self.name = name
        self.share = share
        self.id = id
    }
    
    private override init() {
        super.init()
    }
}
