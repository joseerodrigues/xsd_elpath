xsd_elpath
==========

Prints the full path and type of all the elements in a xsd file.

Additionally, you can also specify a root element from which the paths will be printed.


### Usage as standalone

    java -jar xsd_elpath.jar <xsdFile.xsd> [ElementName]


#### Example

Using [this schema](http://www.w3schools.com/schema/schema_example.asp):

    java -jar xsd_elpath.jar shiporder.xsd

Yields the output:

    /shiporder      null
    /shiporder/item null
    /shiporder/item/note    xs:string
    /shiporder/item/price   xs:decimal
    /shiporder/item/quantity        xs:positiveInteger
    /shiporder/item/title   xs:string
    /shiporder/orderid      xs:string
    /shiporder/orderperson  xs:string
    /shiporder/shipto       null
    /shiporder/shipto/address       xs:string
    /shiporder/shipto/city  xs:string
    /shiporder/shipto/country       xs:string
    /shiporder/shipto/name  xs:string

And if we only want to print the "item" element:

    java -jar xsd_elpath.jar shiporder.xsd item

Output:

    /item   null
    /item/note      xs:string
    /item/price     xs:decimal
    /item/quantity  xs:positiveInteger
    /item/title     xs:string