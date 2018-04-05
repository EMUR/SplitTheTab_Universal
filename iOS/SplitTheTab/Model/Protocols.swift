//
//  Protocols.swift
//  SplitTheTab
//
//  Created by Andrea Vitek on 3/6/18.
//  Copyright © 2018 Eyad. All rights reserved.
//

import Foundation
import KRAlertController

protocol ReloadDelegate: class {
    func reloadTable(owingOrOwed: ReloadDelegateOptions)
}

protocol ProfileCellTappedDelegate: class {
    func cellTapped(tabID: String, cell: ProfileTabsTableViewCell)
}

protocol AddSplitterDelegate: class {
    func addSplitter(withEmail email: String, forID id: Int)
}

protocol CustomAmountDelegate: class {
    func setCusotmAmount(withAmount amount: Float, forID id: Int)
    func presentCustom(withAlert alert: KRAlertController, forID id: Int)
    func addSplitter(forID id: Int)
}
