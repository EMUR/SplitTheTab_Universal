//
//  KRAlertControllerEnumerates.swift
//  KRAlertController
//
//  Copyright © 2016年 Krimpedance. All rights reserved.
//

import UIKit

/**
 Constants indicating the type of alert to display.
 
 - Alert:       An alert displayed modally for the app.
 - ActionSheet: An action sheet displayed in the context of the view controller that presented it.
 */
public enum KRAlertControllerStyle {
   case alert, actionSheet
}

/**
 Styles to apply to action buttons in an alert.
 
 - Default:     Apply the default style to the action’s button.
 - Cancel:      Apply a style that indicates the action cancels the operation and leaves things unchanged.
 - Destructive: Apply a style that indicates the action might change or delete data.
 */
public enum KRAlertActionStyle: Int {
   case `default`
   case cancel
   case destructive
}

/**
 Styles to apply to labels in an alert
 
 - Title:   Title label
 - Message: Message label
 */
enum KRAlertLabelStyle {
   case title, message
}

/**
 Type to apply to style of action buttons in an alert.
 
 - Vertical:      Action buttons are displaying in line.
 - VerticalTable: Action buttons are displaying in line with table view.
 - Horizontal:      Action buttons are displaying in a row.
 */
enum KRButtonLayoutType {
   case vertical
   case verticalTable
   case horizontal
}

/**
 Constants indicating the color of alert to display. Alert icon is indicated by `icon` parameter.
 
 - Normal:      Display black text. This is default.
 - Success:     Display green text and success icon.
 - Information:     Display blue text and information icon.
 - Warning:     Display yellow text and warning icon.
 - Error:     Display red text and error icon.
 - Edit:     Display purple text and edit icon.
 - Authorize:     Display purple text and authorize icon.
 */
enum KRAlertControllerType {
   case normal
   case success(icon: Bool)
   case information(icon: Bool)
   case warning(icon: Bool)
   case error(icon: Bool)
   case edit(icon: Bool)
   case authorize(icon: Bool)

   var textColor: UIColor {
      switch self {
      case .normal: return .black
      case .success: return UIColor ( red: 0.1843, green: 0.3922, blue: 0.1804, alpha: 1.0 )
      case .information: return UIColor ( red: 0.1922, green: 0.4392, blue: 0.5608, alpha: 1.0 )
      case .warning: return UIColor ( red: 0.4627, green: 0.3529, blue: 0.1765, alpha: 1.0 )
      case .error: return UIColor ( red: 0.5882, green: 0.1882, blue: 0.1961, alpha: 1.0 )
      case .edit: return UIColor ( red: 0.5176, green: 0.2431, blue: 0.5922, alpha: 1.0 )
      case .authorize: return UIColor ( red: 0.5961, green: 0.3373, blue: 0.6588, alpha: 1.0 )
      }
   }

   var buttonBackgroundColor: UIColor {
      switch self {
      case .normal: return UIColor ( red: 0.8902, green: 0.8902, blue: 0.898, alpha: 1.0 )
      case .success: return UIColor ( red: 0.8745, green: 0.9412, blue: 0.8471, alpha: 1.0 )
      case .information: return UIColor ( red: 0.851, green: 0.9294, blue: 0.9686, alpha: 1.0 )
      case .warning: return UIColor ( red: 0.9882, green: 0.9725, blue: 0.8902, alpha: 1.0 )
      case .error: return UIColor ( red: 0.949, green: 0.8706, blue: 0.8706, alpha: 1.0 )
      case .edit: return UIColor ( red: 0.9333, green: 0.8549, blue: 0.949, alpha: 1.0 )
      case .authorize: return UIColor ( red: 0.9333, green: 0.8549, blue: 0.949, alpha: 1.0 )
      }
   }

   var iconColor: UIColor {
      switch self {
      case .normal: return .black
      case .success: return UIColor ( red: 0.8039, green: 0.898, blue: 0.7412, alpha: 1.0 )
      case .information: return UIColor ( red: 0.7373, green: 0.8863, blue: 0.9294, alpha: 1.0 )
      case .warning: return UIColor ( red: 0.949, green: 0.902, blue: 0.7529, alpha: 1.0 )
      case .error: return UIColor ( red: 0.9294, green: 0.7725, blue: 0.7725, alpha: 1.0 )
      case .edit: return UIColor ( red: 0.9059, green: 0.8078, blue: 0.9294, alpha: 1.0 )
      case .authorize: return UIColor ( red: 0.9059, green: 0.8078, blue: 0.9294, alpha: 1.0 )
      }
   }

   var isShowIcon: Bool {
      switch self {
      case let .success(icon: isShow): return isShow
      case let .information(icon: isShow): return isShow
      case let .warning(icon: isShow): return isShow
      case let .error(icon: isShow): return isShow
      case let .edit(icon: isShow): return isShow
      case let .authorize(icon: isShow): return isShow
      default: return false
      }
   }

   func getIconLayer(rect: CGRect) -> CAShapeLayer {
      let layer = CAShapeLayer()
      layer.frame = rect
      layer.path = KRAlertIconPath.getPath(self, size: rect.size)
      layer.fillColor = iconColor.cgColor
      return layer
   }
}
