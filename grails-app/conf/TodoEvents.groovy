import todosample.Todo

events = {
    'afterInsert' browser:true, filter:Todo
    'afterDelete' browser:true, filter:Todo
    'afterUpdate' browser:true, filter:Todo
    deployStart browser: true
    deployEnd browser: true
    packageWarStart browser: true
    packageWarStatus browser: true
    packageWarEnd browser: true
}
