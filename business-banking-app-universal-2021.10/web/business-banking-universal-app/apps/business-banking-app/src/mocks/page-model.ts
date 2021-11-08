import { Container, Item } from '@backbase/foundation-ang/web-sdk';
import * as modelMock from '../../../../dist/apps/business-banking-app/page-model-mock.json';

export function processRootLink(nodes: Item[]): Item[] {
  return nodes.map((originalNode) => {
    const node = { ...originalNode };
    if (node.properties?.rootLink) {
      try {
        const rootLink = JSON.parse(node.properties?.rootLink as string);
        node.properties.rootLink = JSON.stringify({ ...rootLink, link: { ...rootLink.link, uuid: rootLink.link.url } });
      } catch (e) {}
    }

    const container = node as Container;
    if (Array.isArray(container.children) && container.children.length) {
      container.children = processRootLink(container.children);
    }
    return node;
  });
}

export const pageModel: Item = {
  name: 'bb-business-banking-app-ang-_-318da3ee68f5',
  properties: {
    'render.requires': 'render-bb-ssr',
    src: '/api/portal/static/items/bb-business-banking-app-ang/index.hbs',
    'render.strategy': 'render-bb-widget-3',
    label: 'Business Banking App Container',
    title: 'Business Banking App Container',
    thumbnailUrl: '/api/portal/static/items/bb-business-banking-app-ang/icon.png',
    'AppConfig.batches': true,
    'AppConfig.contacts': true,
    area: '0',
    order: 8,
  },
  children: processRootLink(modelMock.children[0].children as Item[]),
};
