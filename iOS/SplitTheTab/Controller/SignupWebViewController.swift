//
//  SignupWebViewController.swift
//  SplitTheTab
//
//  Created by Eyad Murshid on 3/4/18.
//  Copyright © 2018 Eyad. All rights reserved.
//

import UIKit
import WebKit
import Firebase
import Alamofire

class SignupWebViewController: UIViewController, WKNavigationDelegate {
    @IBOutlet weak var wview: WKWebView!
    var userEmail:String = ""
    var userPassowrd:String = ""
    var signupNow:Bool!

    override func viewDidLoad() {
        super.viewDidLoad()
        signupNow = true
        wview.navigationDelegate = self
        wview.load(.init(url: URL.init(string: "https://connect.stripe.com/express/oauth/authorize?redirect_uri=https://stripe.com/connect/default/oauth/test&client_id=ca_Bo2VATsQS7g1EMN5B5ZewpTDJvkhrKih&email=\(userEmail)")!))
    }
    
    func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        print(webView.url!.absoluteString)
        
        var str = webView.url!.absoluteString
        
        if str.range(of: "ac_") != nil{
            str.removeSubrange(str.startIndex..<str.range(of: "ac_")!.lowerBound)
            postToFirebase(stripeAcc: str)
        }

        decisionHandler(.allow)
    }
    
    func postToFirebase(stripeAcc:String!){
        if signupNow {
            signupNow = false
            
            Alamofire.request(URL.init(string: "https://connect.stripe.com/oauth/token")!, method: HTTPMethod.post, parameters: ["client_secret":"sk_test_hHnZqkTRUgfhd3sRbMpZ4UX7", "code":stripeAcc, "grant_type":"authorization_code"], encoding: JSONEncoding.default, headers: nil).responseJSON(completionHandler: { (response) in
                var str = response.description
                if str.range(of: "acct_") != nil{
                    str.removeSubrange(str.startIndex..<str.range(of: "acct_")!.lowerBound)
                    str.removeSubrange(str.range(of: "\";")!.lowerBound..<str.endIndex)
                }

                DatabaseHandler.shared().addUserWithStripeAccount(StripeAccount: str)
            })
            
            self.dismiss(animated: true, completion: nil)
        }
    }
    
    @IBAction func back(_ sender: Any) {
        self.navigationController?.popViewController(animated: true)
    }
}
