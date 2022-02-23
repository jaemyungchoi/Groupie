import React from 'react';
import ReactDOM from 'react-dom';
import ProposalDetail from './proposalDetail';

const widget = document.getElementById("proposal");

ReactDOM.render(
  <React.StrictMode>
    <ProposalDetail proposal={widget.dataset.proposal} uid={widget.dataset.uid} error={widget.dataset.error} />
  </React.StrictMode>,
  widget
);
