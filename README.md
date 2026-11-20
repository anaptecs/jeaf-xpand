# JEAF Xpand

Runtime of the template language Xpand and the expression / extension language Xtend (incl. Check) as used by [JEAF Generator](https://www.jeaf-generator.io).

The code is based on [Eclipse M2T Xpand](https://projects.eclipse.org/projects/modeling.m2t.xpand) 2.2.0 (build `R201605260315`), which is not maintained anymore. Eclipse Xpand is the successor of openArchitectureWare 4.3.

## Modules

| Artifact (`com.anaptecs.jeaf.xpand`) | Content |
| ------------------------------------ | ------- |
| `xtend`                  | Expression language, Xtend and Check |
| `xpand`                  | Template language Xpand |
| `xtend-typesystem-emf`   | EMF type system |
| `xtend-typesystem-uml2`  | UML2 type system incl. UML profiles |
| `xtend-util-stdlib`      | Standard library (e.g. `org::eclipse::xtend::util::stdlib::io`) |

Package names are the ones of Eclipse Xpand, so existing templates, extensions and checks work without changes.

## Differences to Eclipse Xpand 2.2.0

- Sources and resources are taken unchanged from the source and binary bundles of the Eclipse Xpand 2.2.0 update site.
- Built with Maven as plain JAR files (Java 17) instead of OSGi bundles. Eclipse signatures and OSGi manifests are not included.
- Only the runtime is contained. Editors for Eclipse are available on the [Eclipse Xpand update site](https://download.eclipse.org/modeling/m2t/xpand/updates/releases/).

## License

Eclipse Public License 1.0 (see [LICENSE](LICENSE)), as Eclipse Xpand itself.
