//
//  LoadingViewController.swift
//  SplitTheTab
//
//  Created by Eyad Murshid on 3/6/18.
//  Copyright © 2018 Eyad. All rights reserved.
//

import UIKit
import NVActivityIndicatorView

class LoadingViewController: UIViewController {
    @IBOutlet weak var loading: NVActivityIndicatorView!
    @IBOutlet weak var loadLabel: UILabel!
    var loadingText = ""
    
    override func viewDidLoad() {
        super.viewDidLoad()
        loading.type = NVActivityIndicatorType.ballGridPulse
        loading.startAnimating()
        loadLabel.text = loadingText

        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func dismissView(completion: @escaping () -> Void){
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            self.dismiss(animated: false, completion: {
                completion()
            })
        }
    }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
