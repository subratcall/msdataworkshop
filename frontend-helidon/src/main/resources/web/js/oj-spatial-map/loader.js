  define(['ojs/ojcomposite', 'text!./oj-spatial-map-view.html', './oj-spatial-map-viewModel', 'text!./component.json', 'css!./styles'],
    function(Composite, view, viewModel, metadata) {
      Composite.register('oj-spatial-map', {
        view: view,
        viewModel: viewModel,
        metadata: JSON.parse(metadata)
      });
    }
  );