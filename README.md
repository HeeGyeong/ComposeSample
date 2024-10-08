# ComposeSample

Jetpack Compose를 공부하고 실무에 적용하면서 발생했던 이슈와 자주 사용되는 다양한 기능들의 샘플을 추가하고 있습니다.

기존에 공부하면서 만들어둔 샘플코드 기반으로 작성되어 MainActivity에서는 가장 기본적인 Compose 사용에 대한 예제가 작성 되어 있으며, 
BlogExampleActivity 에서 블로그에 작성된 실무에 적용할 수 있는 다양한 기능을 구현해 두었습니다.

실제 확인 가능한 UI에서는 버튼을 통해 기본적인 Compose 사용 예제와 블로그 예제를 나눠 들어갈 수 있습니다.

실무에서 사용 할 수 있는 다양한 샘플을 작성해 두었지만, 샘플을 실행하기 위한 모든 코드가 작성되어있지 않습니다.
예로 들어, Permission Check에 관련된 로직은 별도로 추가하지 않았기 때문에, Permission이 필요한 예제인 경우 샘플 앱을 설치한 후, 직접 해당 권한을 설정해 주어야 정상 동작 합니다.
Permission을 위한 로직 등 기본적인 로직이 들어있는 프로젝트라면, 해당 로직을 그대로 가져다 사용해도 사용할 수는 있게 코드를 작성하고 있습니다.

24년 4월 기준, BlogExampleActivity 에서 바로 해당 예제의 블로그 글로 이동할 수 있도록 UI를 변경하였습니다.

24년 6월 기준, Compose 환경에서 CleanArchitecture에 대한 적용을 해보기 위하여 ComposeSample 프로젝트의 일반적인 구조를 CleanArchitecture 구조로 변경하였습니다.
실제로 필요하지 않는 부분이더라도 클린 아키텍처의 구조에 맞춰서 나누어서 정석대로 적용하였습니다.

24년 8월 기준, Kotlin version 1.9.0, AGP version 8.5.2, java version 17, compose version 1.5.2 버전으로 Update 및 버전 대응하였습니다.

실무를 진행하면서 Compose에 대한 스터디를 진행하고 있으며, 관련된 내용들은 필요시 마다 코드를 작성하고 정리하여 블로그에 작성하고 있습니다.
블로그 글과 더불어 샘플 코드만 읽어도 쉽게 기능을 이해하고 사용할 수 있도록 구현하려 노력중입니다.


[Tistory Blog - Compose](https://heegs.tistory.com/category/Android/Jetpack "JetPack Compose")
