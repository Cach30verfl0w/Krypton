# Krypton Cryptography Suite
Krypton is a modular and extensible library suite for cryptography in Kotlin/Multiplatform with support for Linux, Windows, macOS, Android and iOS based on more-tested libraries in the backend like OpenSSL or the JCA. Below this text you can see a list of the modules of Krypton:
- **krypton-core:** Core components of the Krypton library (Algorithm interface etc.)
- **krypton-asn1:** Cross-platform ASN.1 binary format parser with support for [`kotlinx.serialization`](https://github.com/Kotlin/kotlinx.serialization)
- **krypton-keystore:** Implementation of Native keystores with support for parsing Java Keystores on different platforms
- **krypton-openssl:** Windows, Linux, macOS and iOS bindings for [OpenSSL](https://github.com/openssl/openssl) [(Apache License 2.0)](https://github.com/openssl/openssl/blob/master/LICENSE.txt)

The default implementation of Krypton supports different algorithms. Below this text you can see a list of the by-default implemented algorithms:
- **Cryptographic hash functions:** SHA3 (224, 256, 384 and 512 bits), SHA (224, 256, 384 and 512 bits) and MD5
- **Symmetric encryption algorithms:** AES, DES and Triple-DES
- **Asymmetric encryption algorithms:** RSA
- **Key Agreements:** Diffie-Hellman (DH) and Elliptic Curve Diffie-Hellman (ECDH)
- **Signature algorithms:** RSA and ECDSA

## Credits
Some parts of this project are based on the work of other great people. In this part of the README I want to thank them and show a list of my inspirations etc.
- [trixnity-openssl-binaries](https://gitlab.com/trixnity/trixnity-openssl-binaries) - The OpenSSL binaries are acquired by the publications of this repository
- [trixnity-crypto-core](https://gitlab.com/trixnity/trixnity/-/tree/main/trixnity-crypto-core?ref_type=heads) - The integration of OpenSSL over multiple targets is heavily inspired that builscript code
- [A Layman's Guide to a Subset of ASN.1, BER, and DER](https://luca.ntop.org/Teaching/Appunti/asn1.html) - Used for the implementation of the ASN.1 parser

### Dependencies
Also, a few dependencies are needed to make this project work. Below this text you can see a list of these project with author and license (by the time the dependency was added):

| Name                                                                           | Author                                                      | License                                                                                              |
|--------------------------------------------------------------------------------|-------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| [Kotest](https://github.com/kotest/kotest)                                     | [Kotest](https://github.com/kotest)                         | [Apache License 2.0](https://github.com/kotest/kotest/blob/master/LICENSE)                           |
| [bouncycastle-java](https://www.bouncycastle.org/repositories/bc-java)         | [Legion of the Bouncycastle Inc.](https://github.com/bcgit) | [MIT License](https://github.com/bcgit/bc-java/blob/main/LICENSE.md)                                 |
| [dokka](https://github.com/Kotlin/dokka)                                       | [Kotlin](https://github.com/Kotlin)                         | [Apache License 2.0](https://github.com/Kotlin/dokka/blob/master/LICENSE.txt)                        |
| [OpenSSL](https://github.com/OpenSSL/OpenSSL)                                  | [The OpenSSL Project](https://github.com/OpenSSL/OpenSSL)   | [Apache License 2.0](https://github.com/openssl/openssl/blob/master/LICENSE.txt)                     |
| [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)       | [Kotlin](https://github.com/Kotlin)                         | [Apache License 2.0](https://github.com/Kotlin/kotlinx.serialization/blob/master/LICENSE.txt)        |
| [gradle-download-task](https://github.com/michel-kraemer/gradle-download-task) | [Michel Krämer](https://github.com/michel-kraemer)          | [Apache License 2.0](https://github.com/michel-kraemer/gradle-download-task/blob/master/LICENSE.txt) |

## License
This project is licensed with the Apache-2.0 License.
```
Copyright 2025 Cedric Hammes
  
Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 
  
 http://www.apache.org/licenses/LICENSE-2.0 
  
Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
```
