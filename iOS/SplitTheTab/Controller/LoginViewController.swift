//
//  LoginViewController.swift
//  SplitTheTab
//
//  Created by Eyad Murshid on 1/20/18.
//  Copyright © 2018 Eyad. All rights reserved.
//

import UIKit
import FirebaseAuth
import Alamofire

class LoginViewController: UIViewController {
    @IBOutlet weak var emailField: UITextField!
    @IBOutlet weak var passwordField: UITextField!
    @IBOutlet weak var signInButton: UIButton!
    var loadingVC: LoadingViewController!
    
    override func viewWillAppear(_ animated: Bool) {
        loadingVC = UIStoryboard.init(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "Loading") as! LoadingViewController
        loadingVC.loadingText = "Signing in"
    }

    @IBAction func signIn(_ sender: UIButton) {
        self.present(loadingVC, animated: false, completion: nil)
        
        if var username = emailField.text, var password = passwordField.text {
            // TODO: for testing purposes only
            if sender.titleLabel!.text != "Sign In"{
                username = sender.titleLabel!.text!
                password = "testtest"
            }
            
            Auth.auth().signIn(withEmail: username, password: password) { (user, error) in
                if error == nil {
                    // Reset database
                    self.loadingVC.dismissView(completion: {
                        print("Called")
                        DatabaseHandler.shared().resetDatabase()
                        self.dismiss(animated: true, completion: nil)
                    })
                }
            }
        }
    }
  
    
    func signupStripe(){
        let urlString = "https://connect.stripe.com/oauth/token"
        
        Alamofire.request(urlString, method: .post, parameters: ["client_secret": "sk_test_hHnZqkTRUgfhd3sRbMpZ4UX7"],encoding: JSONEncoding.default, headers: nil).responseJSON {
            response in
            switch response.result {
            case .success:
                print(response)
                
                break
            case .failure(let error):
                
                print(error)
            }
        }

    }
    @IBAction func cancelLogin(_ sender: Any) {
        self.dismiss(animated: true, completion: nil)
    }
    // TODO: more complex logic
    private func isPasswordValid(password: String) -> Bool {
        return password.count >= 6 && password.count <= 10
    }
    
    // TODO: more complex logic
    private func isUsernameValid(username: String) -> Bool {
        return username.count >= 1 && username.count <= 10 && username.trimmingCharacters(in: NSCharacterSet.whitespacesAndNewlines) != ""
    }
    
    @IBAction func signupButtonPressed(_ sender: UIButton) {
        if let email = emailField.text, let password = passwordField.text {
            Auth.auth().createUser(withEmail: email, password: password, completion: { (newUser, error) in
                if error == nil {
                    // Reset database
                    DatabaseHandler.shared().resetDatabase()
                    self.performSegue(withIdentifier: "webview", sender: email)
//                    self.dismiss(animated: true, completion: nil)
                    
                } else {
                    let alertController = UIAlertController(title: "Error", message: "Something went wrong while creating the user", preferredStyle: .alert)
                    
                    let defaultAction = UIAlertAction(title: "OK", style: .cancel, handler: nil)
                    alertController.addAction(defaultAction)
                    
                    self.present(alertController, animated: true, completion: nil)
                }
            })
            
        } else {
            // TODO: check for valid email/password
            let alertController = UIAlertController(title: "Error", message: "Please enter an email and password combination to sign up", preferredStyle: .alert)
            
            let defaultAction = UIAlertAction(title: "OK", style: .cancel, handler: nil)
            alertController.addAction(defaultAction)
            
            present(alertController, animated: true, completion: nil)
        }
    }
    
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if(segue.identifier == "webview"){
            let vc = segue.destination as! SignupWebViewController
            
            vc.userEmail = emailField.text!
            
        }
    }
}
