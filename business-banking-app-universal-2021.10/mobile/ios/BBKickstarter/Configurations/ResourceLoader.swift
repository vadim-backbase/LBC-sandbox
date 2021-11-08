//
//  Created by Backbase R&D B.V. on 02/04/2021.
//

import Foundation
import FlagKit

struct ResourceLoader { }

extension ResourceLoader {
    struct Country {
        static var imageHandler: (String) -> UIImage? = { code in
            Flag(countryCode: String(code.prefix(2)))?.originalImage
        }

        static var nameHandler: (String) -> (String?) = { code in
            Locale.autoupdatingCurrent.localizedString(forRegionCode: code)
        }
    }
}

extension ResourceLoader {
    struct Currency {
        static var imageHandler: (String) -> UIImage? = { code in
            Flag(countryCode: String(code.prefix(2)))?.originalImage
        }

        static var nameHandler: (String) -> (String?) = { code in
            Locale.autoupdatingCurrent.localizedString(forCurrencyCode: code)
        }
    }
}
