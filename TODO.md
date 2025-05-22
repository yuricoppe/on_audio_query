# TODO - Melhorias e Próximos Passos para on_audio_query

## Pós-Atualização JDK 17 e Correções de Build

- [ ] **Refatorar `QueryHelper.kt` com Funções de Extensão:**
  - Introduzir funções de extensão para `Cursor` (ex: `fun Cursor.getIntOrNull(columnName: String): Int?`, `fun Cursor.getStringOrNull(columnName: String): String?`, `fun Cursor.getLongOrNull(columnName: String): Long?`) para tornar o `QueryHelper.kt` mais limpo, seguro e fácil de manter.
  - Aplicar essas funções de extensão em todos os locais onde dados são lidos do cursor, especialmente em `loadFirstItem` e revisando as correções já feitas em `loadSongItem`, `loadAlbumItem`, etc.

- [ ] **Tratar Avisos de Depreciação no Código Android Nativo:**
  - Investigar os numerosos avisos sobre APIs depreciadas (ex: `MediaStore.Audio.Playlists.Members`, `MediaStore.Audio.Genres.Members.getContentUri`, `Playlists.EXTERNAL_CONTENT_URI`, etc.) nos logs de compilação.
  - Adotar APIs mais recentes do `MediaStore` para garantir a compatibilidade futura e o bom funcionamento em versões mais novas do Android.

- [ ] **Suprimir Aviso do `compileSdk` (Opcional mas Recomendado para Limpeza de Log):**
  - Adicionar `android.suppressUnsupportedCompileSdk=35` aos seguintes arquivos `gradle.properties` para limpar o log de build:
    - `packages/on_audio_query_android/android/gradle.properties`
    - `packages/on_audio_query/example/android/gradle.properties` (Verificar se já não está no `app/gradle.properties` ou se precisa ser no do projeto exemplo raiz Android).

- [ ] **Remover Atributo `package` dos `AndroidManifest.xml` Restantes:**
  - Os logs de build ainda mostram avisos sobre o atributo `package` em `AndroidManifest.xml`.
  - Verificar e remover o atributo `package` da tag `<manifest>` nos seguintes arquivos (e quaisquer outros onde o aviso apareça):
    - `packages/on_audio_query/example/android/app/src/main/AndroidManifest.xml`
    - `packages/flutter/packages/integration_test/android/src/main/AndroidManifest.xml` (Este pode ser de uma dependência do Flutter SDK, verificar se a alteração é apropriada ou se deve ser ignorado/reportado ao Flutter).

- [ ] **Testes Funcionais Extensivos:**
  - Testar exaustivamente todas as funcionalidades do plugin `on_audio_query` no aplicativo de exemplo.
  - Realizar testes em diferentes versões do Android (físicas e emuladores) para garantir que nenhuma regressão foi introduzida pelas atualizações de JDK, Gradle e correções de código.

- [ ] **Revisar e Reverter Alterações no `pubspec.yaml` (Antes de Publicar Novas Versões):**
  - O arquivo `packages/on_audio_query/pubspec.yaml` foi modificado para usar uma dependência de caminho local para `on_audio_query_android` (`path: ../on_audio_query_android`).
  - **Importante:** Antes de publicar uma nova versão do plugin `on_audio_query` no pub.dev, esta alteração **precisa ser revertida**. A dependência `on_audio_query_android` deve voltar a ser uma dependência de versão publicada (ex: `on_audio_query_android: ^1.x.y`).
  - Isso pressupõe que uma nova versão do `on_audio_query_android` (com as correções do JDK 17 e Lint) também seja publicada, ou que a versão existente já seja compatível.

- [ ] **Considerar Atualização do Android Gradle Plugin (AGP):**
  - O AGP `8.2.2` atual exibe um aviso sobre o `compileSdk 35`. Quando uma versão mais recente do AGP que suporte oficialmente `compileSdk 35` ou superior for lançada, considerar a atualização nos arquivos `build.gradle` relevantes (principalmente a versão do plugin no `settings.gradle`).

- [ ] **Revisar Código Kotlin para Features do JDK 17 (Opcional):**
   - Com o JDK 17, novas features da linguagem Java e Kotlin podem estar disponíveis. Uma revisão do código pode identificar oportunidades para modernizar ou simplificar o código. 

## Análise Geral do Código (Pós-Refatoração Inicial)

- [x] **Corrigir Bug de Recursão em `renamePlaylist`:**
  - **Local:** `packages/on_audio_query/lib/src/on_audio_query.dart`
  - **Problema:** O método `renamePlaylist` chama a si mesmo recursivamente.
  - **Correção:** Alterar `return renamePlaylist(playlistId, newName);` para `return platform.renamePlaylist(playlistId, newName);`.
  - **Status:** ✅ Corrigido em commit 92681c8

- [ ] **Revisar Tratamento de Erros Nativo->Dart:**
  - Investigar se todos os cenários de erro potenciais no código nativo (Kotlin, Swift) são capturados e comunicados de volta ao Dart de forma consistente, utilizando códigos de erro específicos quando apropriado para permitir um tratamento mais granular por parte dos usuários do plugin.

- [ ] **Expandir Cobertura de Testes:**
  - Avaliar e expandir os testes unitários e de integração (incluindo testes de plataforma) para cobrir todos os métodos públicos, cenários de uso, casos de borda e diferentes configurações de permissão.

- [ ] **Monitorar Tamanho/Complexidade de `OnAudioQuery` e `MethodController` (Kotlin):**
  - **`packages/on_audio_query/lib/src/on_audio_query.dart`:** Se o número de métodos continuar a crescer significativamente, considerar refatorar agrupando funcionalidades relacionadas em classes menores (ex: `PlaylistManager`).
  - **`MethodController.kt` (Android):** Se esta classe se tornar muito grande, aplicar o mesmo princípio de dividir em classes menores por funcionalidade (ex: `SongQueryHandler`, `AlbumQueryHandler`).

- [ ] **Manter Documentação de Suporte de Plataforma Atualizada:**
  - Continuar garantindo que a tabela "Platforms" na documentação de cada método reflita precisamente o suporte atual, especialmente para funcionalidades com suporte limitado. 